package com.example.library.interlibrary;

import com.example.library.entity.LibraryBranch;
import com.example.library.entity.Member;
import com.example.library.repository.LibraryBranchRepository;
import com.example.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterLibraryLoanService {

    private final InterLibraryLoanRepository illRepository;
    private final PartnerLibraryRepository partnerLibraryRepository;
    private final MemberRepository memberRepository;
    private final LibraryBranchRepository branchRepository;

    @Transactional
    public InterLibraryLoanDTO submitRequest(SubmitILLRequest request) {
        log.info("Submitting ILL request for member {} book '{}'", request.getMemberId(), request.getBookTitle());

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + request.getMemberId()));

        LibraryBranch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Library branch not found with id: " + request.getBranchId()));

        if (request.getBookTitle() == null || request.getBookTitle().isBlank()) {
            throw new RuntimeException("Book title must not be blank");
        }

        InterLibraryLoanRequest illRequest = InterLibraryLoanRequest.builder()
                .requestingMember(member)
                .requestingBranch(branch)
                .bookTitle(request.getBookTitle())
                .authorName(request.getAuthorName())
                .isbn(request.getIsbn())
                .requestDate(LocalDate.now())
                .neededByDate(request.getNeededByDate())
                .notes(request.getNotes())
                .status(ILLStatus.PENDING)
                .build();

        InterLibraryLoanRequest saved = illRepository.save(illRequest);
        log.info("ILL request created with id {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public InterLibraryLoanDTO approveRequest(Long requestId, String staffName, String note) {
        log.info("Approving ILL request {}", requestId);

        InterLibraryLoanRequest request = findById(requestId);

        if (request.getStatus() != ILLStatus.PENDING) {
            throw new RuntimeException("Request is not in PENDING status");
        }

        request.setStatus(ILLStatus.APPROVED);
        request.setReviewedByName(staffName);
        request.setReviewNote(note);

        return toDTO(illRepository.save(request));
    }

    @Transactional
    public InterLibraryLoanDTO orderFromPartner(Long requestId, Long partnerLibraryId) {
        log.info("Ordering ILL request {} from partner library {}", requestId, partnerLibraryId);

        InterLibraryLoanRequest request = findById(requestId);

        if (request.getStatus() != ILLStatus.APPROVED) {
            throw new RuntimeException("Request must be APPROVED before ordering");
        }

        PartnerLibrary partner = partnerLibraryRepository.findById(partnerLibraryId)
                .orElseThrow(() -> new RuntimeException("Partner library not found with id: " + partnerLibraryId));

        request.setStatus(ILLStatus.ORDERED);
        request.setPartnerLibrary(partner);
        request.setEstimatedArrival(LocalDate.now().plusDays(partner.getShippingDays()));

        return toDTO(illRepository.save(request));
    }

    @Transactional
    public InterLibraryLoanDTO markReceived(Long requestId) {
        log.info("Marking ILL request {} as RECEIVED", requestId);

        InterLibraryLoanRequest request = findById(requestId);
        request.setStatus(ILLStatus.RECEIVED);
        request.setEstimatedArrival(LocalDate.now());

        return toDTO(illRepository.save(request));
    }

    @Transactional
    public InterLibraryLoanDTO markAvailable(Long requestId) {
        log.info("Marking ILL request {} as AVAILABLE", requestId);

        InterLibraryLoanRequest request = findById(requestId);
        request.setStatus(ILLStatus.AVAILABLE);

        return toDTO(illRepository.save(request));
    }

    @Transactional
    public InterLibraryLoanDTO processReturn(Long requestId) {
        log.info("Processing return for ILL request {}", requestId);

        InterLibraryLoanRequest request = findById(requestId);
        request.setStatus(ILLStatus.RETURNED);

        return toDTO(illRepository.save(request));
    }

    @Transactional
    public InterLibraryLoanDTO denyRequest(Long requestId, String staffName, String reason) {
        log.info("Denying ILL request {}", requestId);

        InterLibraryLoanRequest request = findById(requestId);

        if (request.getStatus() != ILLStatus.PENDING) {
            throw new RuntimeException("Request is not in PENDING status");
        }

        request.setStatus(ILLStatus.DENIED);
        request.setReviewedByName(staffName);
        request.setDenialReason(reason);

        return toDTO(illRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<InterLibraryLoanDTO> getMemberRequests(Long memberId) {
        log.info("Fetching ILL requests for member {}", memberId);
        return illRepository.findByRequestingMemberId(memberId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<InterLibraryLoanDTO> getRequestsByStatus(ILLStatus status, Pageable pageable) {
        log.info("Fetching ILL requests with status {}", status);
        return illRepository.findByStatusIn(List.of(status), pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<InterLibraryLoanDTO> getAllRequests(Pageable pageable) {
        log.info("Fetching all ILL requests, page={}", pageable.getPageNumber());
        return illRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<InterLibraryLoanDTO> getPendingRequests() {
        log.info("Fetching all PENDING ILL requests");
        return illRepository.findByStatus(ILLStatus.PENDING).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public InterLibraryLoanDTO getRequestById(Long requestId) {
        return toDTO(findById(requestId));
    }

    private InterLibraryLoanRequest findById(Long requestId) {
        return illRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("ILL request not found with id: " + requestId));
    }

    InterLibraryLoanDTO toDTO(InterLibraryLoanRequest r) {
        String memberName = null;
        if (r.getRequestingMember() != null && r.getRequestingMember().getUser() != null) {
            memberName = r.getRequestingMember().getUser().getFirstName()
                    + " " + r.getRequestingMember().getUser().getLastName();
        }

        Long partnerLibraryId = null;
        String partnerLibraryName = null;
        if (r.getPartnerLibrary() != null) {
            partnerLibraryId = r.getPartnerLibrary().getId();
            partnerLibraryName = r.getPartnerLibrary().getName();
        }

        return InterLibraryLoanDTO.builder()
                .id(r.getId())
                .memberId(r.getRequestingMember() != null ? r.getRequestingMember().getId() : null)
                .memberName(memberName)
                .branchId(r.getRequestingBranch() != null ? r.getRequestingBranch().getId() : null)
                .branchName(r.getRequestingBranch() != null ? r.getRequestingBranch().getName() : null)
                .bookTitle(r.getBookTitle())
                .authorName(r.getAuthorName())
                .isbn(r.getIsbn())
                .requestDate(r.getRequestDate())
                .neededByDate(r.getNeededByDate())
                .status(r.getStatus())
                .partnerLibraryId(partnerLibraryId)
                .partnerLibraryName(partnerLibraryName)
                .notes(r.getNotes())
                .estimatedArrival(r.getEstimatedArrival())
                .reviewedByName(r.getReviewedByName())
                .reviewNote(r.getReviewNote())
                .denialReason(r.getDenialReason())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
