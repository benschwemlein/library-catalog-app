package com.example.library.pattern.specification;

import com.example.library.entity.Book;
import com.example.library.entity.CopyStatus;
import org.springframework.stereotype.Component;

@Component
public class IsAvailableSpecification extends AbstractSpecification<Book> {

    @Override
    public boolean isSatisfiedBy(Book book) {
        if (book.getCopies() == null || book.getCopies().isEmpty()) {
            return false;
        }
        return book.getCopies().stream()
            .anyMatch(copy -> copy.getStatus() == CopyStatus.AVAILABLE);
    }
}
