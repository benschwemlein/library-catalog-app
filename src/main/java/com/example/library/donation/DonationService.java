package com.example.library.donation;

import com.example.library.entity.Book;
import com.example.library.entity.LibraryBranch;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LibraryBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonationItemRepository donationItemRepository;
    private final BookRepository bookRepository;
    private final LibraryBranchRepository libraryBranchRepository;

    @Transactional
    public DonationDTO recordDonation(String donorName,
                                      String donorEmail,
                                      String donorPhone,
                                      String donorAddress,
                                      List<DonationItemDTO> itemDTOs,
                                      Long branchId) {
        if (donorName == null || donorName.isBlank()) {
            throw new IllegalArgumentException("Donor name must not be blank");
        }

        LibraryBranch branch = null;
        if (branchId != null) {
            branch = libraryBranchRepository.findById(branchId)
                    .orElseThrow(() -> new RuntimeException("Library branch not found: " + branchId));
        }

        DonorInfo donorInfo = DonorInfo.builder()
                .donorName(donorName)
                .donorEmail(donorEmail)
                .donorPhone(donorPhone)
                .donorAddress(donorAddress)
                .build();

        Donation donation = Donation.builder()
                .donor(donorInfo)
                .donationDate(LocalDate.now())
                .status(DonationStatus.PENDING_REVIEW)
                .targetBranch(branch)
                .items(new ArrayList<>())
                .build();

        if (itemDTOs != null) {
            for (DonationItemDTO dto : itemDTOs) {
                DonationItem item = DonationItem.builder()
                        .donation(donation)
                        .title(dto.getTitle())
                        .author(dto.getAuthor())
                        .isbn(dto.getIsbn())
                        .quantity(dto.getQuantity() > 0 ? dto.getQuantity() : 1)
                        .condition(dto.getCondition())
                        .disposition(ItemDisposition.PENDING)
                        .build();
                donation.getItems().add(item);
            }
        }

        Donation saved = donationRepository.save(donation);
        log.info("Recorded donation {} from donor '{}'", saved.getId(), donorName);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public DonationDTO getDonation(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found: " + donationId));
        return toDTO(donation);
    }

    @Transactional(readOnly = true)
    public Page<DonationDTO> getDonationsByStatus(DonationStatus status, Pageable pageable) {
        if (status == null) {
            return donationRepository.findAll(pageable).map(this::toDTO);
        }
        return donationRepository.findByStatus(status, pageable).map(this::toDTO);
    }

    @Transactional
    public DonationDTO reviewDonation(Long donationId, String reviewerName, String notes, DonationStatus decision) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found: " + donationId));

        donation.setStatus(decision);
        donation.setReviewedByName(reviewerName);
        donation.setNotes(notes);

        if (decision == DonationStatus.DECLINED) {
            log.info("Donation {} declined by {}", donationId, reviewerName);
        }

        Donation saved = donationRepository.save(donation);
        return toDTO(saved);
    }

    @Transactional
    public DonationItemDTO processItem(Long itemId, ItemDisposition disposition, String notes, Long bookId) {
        DonationItem item = donationItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Donation item not found: " + itemId));

        item.setDisposition(disposition);
        item.setDispositionNotes(notes);

        if (disposition == ItemDisposition.ADDED_TO_COLLECTION) {
            if (bookId != null) {
                Book book = bookRepository.findById(bookId)
                        .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));
                item.setAddedBook(book);
            } else {
                log.info("Item {} added to collection without specific book link - manual catalog entry may be required", itemId);
            }
        }

        DonationItem saved = donationItemRepository.save(item);

        Long donationId = item.getDonation().getId();
        List<DonationItem> allItems = donationItemRepository.findByDonationId(donationId);
        long pendingCount = allItems.stream()
                .filter(i -> i.getDisposition() == ItemDisposition.PENDING)
                .count();

        if (pendingCount == 0 && !allItems.isEmpty()) {
            long addedCount = allItems.stream()
                    .filter(i -> i.getDisposition() == ItemDisposition.ADDED_TO_COLLECTION)
                    .count();

            Donation donation = donationRepository.findById(donationId)
                    .orElseThrow(() -> new RuntimeException("Donation not found: " + donationId));

            if (addedCount == allItems.size()) {
                donation.setStatus(DonationStatus.ACCEPTED);
            } else if (addedCount > 0) {
                donation.setStatus(DonationStatus.PARTIALLY_ACCEPTED);
            }
            // If no items were added to collection, donation status remains as set by reviewDonation (e.g. DECLINED)

            donationRepository.save(donation);
        }

        return toItemDTO(saved);
    }

    @Transactional
    public DonationDTO sendAcknowledgement(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found: " + donationId));

        donation.setAcknowledgementSent(true);
        donation.setAcknowledgementDate(LocalDate.now());

        log.info("Acknowledgement sent to {} at {}",
                donation.getDonor().getDonorName(),
                donation.getDonor().getDonorEmail());

        Donation saved = donationRepository.save(donation);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDonationStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalDonations", donationRepository.count());
        stats.put("pendingReview", donationRepository.countByStatus(DonationStatus.PENDING_REVIEW));
        stats.put("accepted", donationRepository.countByStatus(DonationStatus.ACCEPTED));
        stats.put("partiallyAccepted", donationRepository.countByStatus(DonationStatus.PARTIALLY_ACCEPTED));
        stats.put("declined", donationRepository.countByStatus(DonationStatus.DECLINED));
        stats.put("totalItems", donationItemRepository.count());
        stats.put("addedToCollection", donationItemRepository.countByDisposition(ItemDisposition.ADDED_TO_COLLECTION));
        stats.put("discarded", donationItemRepository.countByDisposition(ItemDisposition.DISCARDED));
        stats.put("donatedElsewhere", donationItemRepository.countByDisposition(ItemDisposition.DONATED_ELSEWHERE));
        stats.put("pendingProcessing", donationItemRepository.countByDisposition(ItemDisposition.PENDING));
        return stats;
    }

    private DonationDTO toDTO(Donation d) {
        List<DonationItemDTO> itemDTOs = d.getItems() == null
                ? List.of()
                : d.getItems().stream().map(this::toItemDTO).collect(Collectors.toList());

        Long branchId = null;
        String branchName = null;
        if (d.getTargetBranch() != null) {
            branchId = d.getTargetBranch().getId();
            branchName = d.getTargetBranch().getName();
        }

        String donorName = null;
        String donorEmail = null;
        String donorPhone = null;
        String donorAddress = null;
        if (d.getDonor() != null) {
            donorName = d.getDonor().getDonorName();
            donorEmail = d.getDonor().getDonorEmail();
            donorPhone = d.getDonor().getDonorPhone();
            donorAddress = d.getDonor().getDonorAddress();
        }

        return DonationDTO.builder()
                .id(d.getId())
                .donorName(donorName)
                .donorEmail(donorEmail)
                .donorPhone(donorPhone)
                .donorAddress(donorAddress)
                .donationDate(d.getDonationDate())
                .status(d.getStatus())
                .reviewedByName(d.getReviewedByName())
                .notes(d.getNotes())
                .acknowledgementSent(d.isAcknowledgementSent())
                .acknowledgementDate(d.getAcknowledgementDate())
                .targetBranchId(branchId)
                .targetBranchName(branchName)
                .items(itemDTOs)
                .itemCount(itemDTOs.size())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private DonationItemDTO toItemDTO(DonationItem i) {
        Long donationId = i.getDonation() != null ? i.getDonation().getId() : null;
        Long addedBookId = i.getAddedBook() != null ? i.getAddedBook().getId() : null;

        return DonationItemDTO.builder()
                .id(i.getId())
                .donationId(donationId)
                .title(i.getTitle())
                .author(i.getAuthor())
                .isbn(i.getIsbn())
                .quantity(i.getQuantity())
                .condition(i.getCondition())
                .disposition(i.getDisposition())
                .dispositionNotes(i.getDispositionNotes())
                .addedBookId(addedBookId)
                .build();
    }
}
