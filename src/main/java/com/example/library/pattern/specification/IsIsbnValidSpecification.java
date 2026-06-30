package com.example.library.pattern.specification;

import com.example.library.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class IsIsbnValidSpecification extends AbstractSpecification<Book> {

    @Override
    public boolean isSatisfiedBy(Book book) {
        String isbn = book.getIsbn();
        if (isbn == null || isbn.isBlank()) {
            return false;
        }
        String digits = isbn.replaceAll("[\\s\\-]", "");
        if (digits.length() != 13) {
            return false;
        }
        if (!digits.matches("\\d{13}")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = digits.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit == (digits.charAt(12) - '0');
    }
}
