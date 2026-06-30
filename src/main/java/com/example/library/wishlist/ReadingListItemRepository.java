package com.example.library.wishlist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReadingListItemRepository extends JpaRepository<ReadingListItem, Long> {

    List<ReadingListItem> findByReadingListIdOrderBySortOrder(Long listId);

    Optional<ReadingListItem> findByReadingListIdAndBookId(Long listId, Long bookId);

    long countByReadingListId(Long listId);

    long countByReadingListIdAndReadTrue(Long listId);
}
