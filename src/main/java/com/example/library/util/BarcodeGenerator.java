package com.example.library.util;

import com.example.library.repository.BookCopyRepository;
import org.springframework.stereotype.Component;

@Component
public class BarcodeGenerator {

    /**
     * Generates a barcode for a book copy.
     * Format: "BC-{branchCode}-{bookId}-{checksum}"
     * The checksum is computed using a simple mod-10 algorithm on the concatenated digits.
     *
     * @param branchCode alphanumeric branch identifier (e.g. "MAIN", "NORTH")
     * @param bookId     the ID of the book
     * @return a formatted barcode string
     */
    public String generateBarcode(String branchCode, Long bookId) {
        if (branchCode == null || branchCode.isBlank()) {
            throw new IllegalArgumentException("Branch code must not be blank");
        }
        if (bookId == null || bookId <= 0) {
            throw new IllegalArgumentException("Book ID must be a positive number");
        }

        String bookIdStr = String.valueOf(bookId);
        int checksum = computeChecksum(branchCode + bookIdStr);
        return String.format("BC-%s-%d-%d", branchCode.toUpperCase(), bookId, checksum);
    }

    /**
     * Validates that a barcode has the correct format and valid checksum.
     *
     * @param barcode the barcode string to validate
     * @return true if the barcode format and checksum are valid
     */
    public boolean validateBarcode(String barcode) {
        if (barcode == null || !barcode.startsWith("BC-")) {
            return false;
        }
        String[] parts = barcode.split("-");
        // Expected: ["BC", branchCode, bookId, checksum]
        if (parts.length != 4) {
            return false;
        }
        try {
            String branchCode = parts[1];
            long bookId = Long.parseLong(parts[2]);
            int providedChecksum = Integer.parseInt(parts[3]);
            int expectedChecksum = computeChecksum(branchCode + bookId);
            return providedChecksum == expectedChecksum;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks whether a barcode is unique by querying the BookCopyRepository.
     *
     * @param barcode the barcode to check
     * @param repo    the repository used to check for existing barcodes
     * @return true if no book copy with this barcode exists
     */
    public boolean isUnique(String barcode, BookCopyRepository repo) {
        return repo.findByBarcode(barcode).isEmpty();
    }

    /**
     * Computes a simple mod-10 checksum from the digits in the input string.
     * Non-digit characters contribute 0.
     *
     * @param input the string to compute checksum from
     * @return a single-digit checksum (0-9)
     */
    private int computeChecksum(String input) {
        int sum = 0;
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                sum += Character.getNumericValue(c);
            }
        }
        return sum % 10;
    }
}
