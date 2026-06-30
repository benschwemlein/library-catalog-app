package com.example.library.repository;

import com.example.library.entity.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, Long> {

    List<BookReview> findByBook_IdAndApproved(Long bookId, boolean approved);

    List<BookReview> findByMember_Id(Long memberId);

    @Query("SELECT AVG(r.rating) FROM BookReview r WHERE r.book.id = :bookId AND r.approved = true")
    Double averageRatingByBook(@Param("bookId") Long bookId);
}
