package com.example.library.digitalresource;

import com.example.library.entity.Member;
import com.example.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DigitalResourceService {

    private final DigitalResourceRepository digitalResourceRepository;
    private final DigitalLicenseRepository digitalLicenseRepository;
    private final DigitalLoanRepository digitalLoanRepository;
    private final MemberRepository memberRepository;

    public Page<DigitalResourceDTO> searchResources(String query, DigitalResourceType type, Pageable pageable) {
        log.debug("Searching digital resources: query='{}', type={}", query, type);

        boolean hasQuery = query != null && !query.isBlank();
        boolean hasType = type != null;

        if (hasQuery && hasType) {
            Page<DigitalResource> byTitle = digitalResourceRepository
                    .findByTitleContainingIgnoreCaseAndActiveTrue(query, pageable);
            List<DigitalResource> filtered = byTitle.getContent().stream()
                    .filter(r -> r.getResourceType() == type)
                    .collect(Collectors.toList());
            Page<DigitalResource> filteredPage = new PageImpl<>(filtered, pageable, filtered.size());
            return filteredPage.map(this::toResourceDTO);
        } else if (hasQuery) {
            return digitalResourceRepository
                    .findByTitleContainingIgnoreCaseAndActiveTrue(query, pageable)
                    .map(this::toResourceDTO);
        } else if (hasType) {
            List<DigitalResource> byType = digitalResourceRepository.findByResourceTypeAndActiveTrue(type);
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), byType.size());
            List<DigitalResource> pageContent = (start > byType.size()) ? List.of() : byType.subList(start, end);
            Page<DigitalResource> page = new PageImpl<>(pageContent, pageable, byType.size());
            return page.map(this::toResourceDTO);
        } else {
            return digitalResourceRepository.findByActiveTrue(pageable).map(this::toResourceDTO);
        }
    }

    public DigitalResourceDTO getResource(Long id) {
        log.debug("Getting digital resource: id={}", id);
        DigitalResource resource = digitalResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Digital resource not found: " + id));
        return toResourceDTO(resource);
    }

    public boolean checkLicenseAvailability(Long resourceId) {
        DigitalResource resource = digitalResourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Digital resource not found: " + resourceId));

        if (resource.getLicenseType() == LicenseType.UNLIMITED) {
            return true;
        }

        long activeCount = digitalLoanRepository.countByResourceAndStatus(resource, DigitalLoanStatus.ACTIVE);
        List<DigitalLicense> activeLicenses = digitalLicenseRepository.findByResourceAndActiveTrue(resource);
        int totalMaxUsers = activeLicenses.stream().mapToInt(DigitalLicense::getMaxUsers).sum();

        return activeCount < totalMaxUsers;
    }

    @Transactional
    public DigitalLoanDTO checkout(Long memberId, Long resourceId) {
        log.info("Checking out digital resource: memberId={}, resourceId={}", memberId, resourceId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found: " + memberId));
        DigitalResource resource = digitalResourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Digital resource not found: " + resourceId));

        if (!resource.isActive()) {
            throw new RuntimeException("Resource is not available");
        }

        boolean alreadyCheckedOut = digitalLoanRepository
                .findByMemberAndStatus(member, DigitalLoanStatus.ACTIVE)
                .stream()
                .anyMatch(loan -> loan.getResource().getId().equals(resourceId));

        if (alreadyCheckedOut) {
            throw new RuntimeException("Member already has an active loan for this resource");
        }

        if (!checkLicenseAvailability(resourceId)) {
            throw new RuntimeException("No licenses available");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = (resource.getLicenseType() == LicenseType.SINGLE_USER)
                ? now.plusDays(7)
                : now.plusDays(14);

        DigitalLoan loan = DigitalLoan.builder()
                .resource(resource)
                .member(member)
                .startDate(now)
                .expiryDate(expiryDate)
                .status(DigitalLoanStatus.ACTIVE)
                .build();

        loan = digitalLoanRepository.save(loan);
        log.info("Digital loan created: loanId={}", loan.getId());
        return toDTO(loan);
    }

    @Transactional
    public DigitalLoanDTO returnResource(Long memberId, Long resourceId) {
        log.info("Returning digital resource: memberId={}, resourceId={}", memberId, resourceId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found: " + memberId));
        DigitalResource resource = digitalResourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Digital resource not found: " + resourceId));

        DigitalLoan loan = digitalLoanRepository.findByMemberAndStatus(member, DigitalLoanStatus.ACTIVE)
                .stream()
                .filter(l -> l.getResource().getId().equals(resourceId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No active loan found for member " + memberId + " and resource " + resourceId));

        loan.setStatus(DigitalLoanStatus.RETURNED);
        loan.setReturnDate(LocalDateTime.now());
        loan = digitalLoanRepository.save(loan);
        log.info("Digital loan returned: loanId={}", loan.getId());
        return toDTO(loan);
    }

    @Transactional
    public DigitalLoanDTO trackDownload(Long loanId) {
        log.info("Tracking download: loanId={}", loanId);

        DigitalLoan loan = digitalLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));

        if (loan.getStatus() != DigitalLoanStatus.ACTIVE) {
            throw new RuntimeException("Loan is not active: " + loanId);
        }

        loan.setDownloadCount(loan.getDownloadCount() + 1);
        loan = digitalLoanRepository.save(loan);
        return toDTO(loan);
    }

    public List<DigitalLoanDTO> getMemberLoans(Long memberId) {
        log.debug("Getting loans for member: memberId={}", memberId);
        return digitalLoanRepository.findByMemberId(memberId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private DigitalResourceDTO toResourceDTO(DigitalResource r) {
        long activeLoans = digitalLoanRepository.countByResourceAndStatus(r, DigitalLoanStatus.ACTIVE);
        boolean availableNow = checkLicenseAvailability(r.getId());

        return DigitalResourceDTO.builder()
                .id(r.getId())
                .title(r.getTitle())
                .description(r.getDescription())
                .resourceType(r.getResourceType())
                .format(r.getFormat())
                .fileUrl(r.getFileUrl())
                .fileSizeBytes(r.getFileSizeBytes())
                .durationMinutes(r.getDurationMinutes())
                .publisher(r.getPublisher())
                .isbn(r.getIsbn())
                .licenseType(r.getLicenseType())
                .maxConcurrentUsers(r.getMaxConcurrentUsers())
                .publicationYear(r.getPublicationYear())
                .language(r.getLanguage())
                .coverImageUrl(r.getCoverImageUrl())
                .active(r.isActive())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .activeLoans((int) activeLoans)
                .availableNow(availableNow)
                .build();
    }

    private DigitalLoanDTO toDTO(DigitalLoan loan) {
        String fileUrl = (loan.getStatus() == DigitalLoanStatus.ACTIVE)
                ? loan.getResource().getFileUrl()
                : null;

        return DigitalLoanDTO.builder()
                .loanId(loan.getId())
                .resourceId(loan.getResource().getId())
                .resourceTitle(loan.getResource().getTitle())
                .memberId(loan.getMember().getId())
                .startDate(loan.getStartDate())
                .expiryDate(loan.getExpiryDate())
                .downloadCount(loan.getDownloadCount())
                .status(loan.getStatus())
                .fileUrl(fileUrl)
                .build();
    }
}
