package com.example.library.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;

@Component
public class MembershipNumberGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RANDOM_BOUND = 900000; // ensures 6-digit range: 100000-999999
    private static final int RANDOM_OFFSET = 100000;

    /**
     * Generates a unique membership number in the format: LIB-{year}-{6-digit number}
     * e.g. "LIB-2024-483921"
     *
     * @return a formatted membership number string
     */
    public String generate() {
        int year = LocalDate.now().getYear();
        int randomPart = SECURE_RANDOM.nextInt(RANDOM_BOUND) + RANDOM_OFFSET;
        return String.format("LIB-%d-%06d", year, randomPart);
    }
}
