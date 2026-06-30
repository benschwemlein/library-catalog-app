package com.example.library.pattern.factory;

import com.example.library.entity.Book;
import com.example.library.entity.BookCopy;
import com.example.library.entity.CopyCondition;
import com.example.library.entity.CopyStatus;
import com.example.library.entity.LibraryBranch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Factory for constructing {@link BookCopy} entities.
 *
 * <p>All methods return a new, unsaved {@link BookCopy}. The caller is responsible
 * for persisting the copy via {@code BookCopyRepository.save()}.</p>
 */
@Component
@Slf4j
public class BookCopyFactory {

    /**
     * Create a standard book copy with an auto-generated barcode.
     *
     * @param book      the book this copy belongs to
     * @param branch    the branch where the copy will be shelved
     * @param condition the physical condition of the copy
     * @return an unsaved BookCopy entity with AVAILABLE status
     */
    public BookCopy createCopy(Book book, LibraryBranch branch, CopyCondition condition) {
        String barcode = generateBarcode(book, branch);

        BookCopy copy = BookCopy.builder()
                .book(book)
                .branch(branch)
                .barcode(barcode)
                .condition(condition)
                .status(CopyStatus.AVAILABLE)
                .acquiredDate(LocalDate.now())
                .build();

        log.debug("Created book copy: barcode={} book='{}' branch='{}' condition={}",
                barcode, book.getTitle(), branch.getName(), condition);
        return copy;
    }

    /**
     * Create a copy acquired via donation.
     * Condition is set to GOOD; the donor name is recorded in a log for cataloguing.
     *
     * @param book      the book this copy belongs to
     * @param branch    the branch that received the donation
     * @param donorName the name of the donor (logged for administrative purposes)
     * @return an unsaved BookCopy entity with AVAILABLE status and GOOD condition
     */
    public BookCopy createDonatedCopy(Book book, LibraryBranch branch, String donorName) {
        BookCopy copy = createCopy(book, branch, CopyCondition.GOOD);

        log.info("Donated copy accepted: book='{}' branch='{}' donor='{}' barcode={}",
                book.getTitle(), branch.getName(), donorName, copy.getBarcode());

        // In a richer domain model, the donor name would be stored in a DonationRecord entity.
        // Here we log it for administrative traceability.
        return copy;
    }

    /**
     * Create a copy acquired via purchase.
     * Condition is set to NEW; the purchase price is logged for budget tracking.
     *
     * @param book          the book this copy belongs to
     * @param branch        the purchasing branch
     * @param purchasePrice the amount paid for the copy
     * @return an unsaved BookCopy entity with AVAILABLE status and NEW condition
     */
    public BookCopy createPurchasedCopy(Book book, LibraryBranch branch, BigDecimal purchasePrice) {
        BookCopy copy = createCopy(book, branch, CopyCondition.NEW);

        log.info("Purchased copy catalogued: book='{}' branch='{}' price=${} barcode={}",
                book.getTitle(), branch.getName(), purchasePrice, copy.getBarcode());

        // In a richer domain model, the purchase price would be stored in an AcquisitionRecord entity.
        return copy;
    }

    /**
     * Generate a unique barcode for a book copy.
     * Format: BC-{branchId}-{bookId}-{8-char UUID segment}
     */
    private String generateBarcode(Book book, LibraryBranch branch) {
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return String.format("BC-%d-%d-%s", branch.getId(), book.getId(), uuidPart);
    }
}
