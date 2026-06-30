package com.example.library.util;

import org.springframework.stereotype.Component;

@Component
public class IsbnValidator {

    /**
     * Normalizes an ISBN by removing hyphens and spaces.
     *
     * @param isbn the raw ISBN string
     * @return normalized ISBN with only digits (and 'X' for ISBN-10)
     */
    public String normalize(String isbn) {
        if (isbn == null) {
            return null;
        }
        return isbn.replaceAll("[\\s-]", "");
    }

    /**
     * Validates an ISBN-13 using the standard check digit algorithm.
     * The check digit is calculated as:
     * sum = sum of (digit * weight) where weight alternates 1, 3
     * checkDigit = (10 - (sum % 10)) % 10
     *
     * @param isbn the ISBN-13 string (may contain hyphens/spaces)
     * @return true if the ISBN-13 is valid
     */
    public boolean validateIsbn13(String isbn) {
        if (isbn == null) {
            return false;
        }
        String normalized = normalize(isbn);
        if (normalized.length() != 13) {
            return false;
        }
        if (!normalized.matches("\\d{13}")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(normalized.charAt(i));
            int weight = (i % 2 == 0) ? 1 : 3;
            sum += digit * weight;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        int lastDigit = Character.getNumericValue(normalized.charAt(12));
        return checkDigit == lastDigit;
    }

    /**
     * Validates an ISBN-10 using the standard check digit algorithm.
     * The check digit is calculated as:
     * sum = sum of (digit * position) for positions 1-9
     * checkDigit = (11 - (sum % 11)) % 11
     * 'X' represents 10 as the check digit.
     *
     * @param isbn the ISBN-10 string (may contain hyphens/spaces)
     * @return true if the ISBN-10 is valid
     */
    public boolean validateIsbn10(String isbn) {
        if (isbn == null) {
            return false;
        }
        String normalized = normalize(isbn);
        if (normalized.length() != 10) {
            return false;
        }
        if (!normalized.matches("\\d{9}[\\dX]")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int digit = Character.getNumericValue(normalized.charAt(i));
            sum += digit * (10 - i);
        }

        char lastChar = normalized.charAt(9);
        int checkValue = (lastChar == 'X') ? 10 : Character.getNumericValue(lastChar);
        sum += checkValue;

        return sum % 11 == 0;
    }
}
