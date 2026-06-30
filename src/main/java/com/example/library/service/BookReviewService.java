package com.example.library.service;

import com.example.library.dto.CreateReviewRequest;
import com.example.library.entity.Book;
import com.example.library.entity.BookReview;
import com.example.library.entity.Member;
import com.example.library.exception.BookNotFoundException;
import com.example.library.exception.MemberNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BookReviewRepository;
import com.example.library.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BookReviewService {

    @Autowired
    private BookReviewRepository bookReviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    public BookReview submitReview(Long bookId, Long memberId, CreateReviewRequest req) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found: " + memberId));

        BookReview review = new BookReview();
        review.setBook(book);
        review.setMember(member);
        review.setRating(req.getRating());
        review.setReviewText(req.getReviewText());
        review.setReviewDate(LocalDateTime.now());
        review.setApproved(false);

        return bookReviewRepository.save(review);
    }

    public BookReview create(Long bookId, Object reviewDTO) {
        // Simple stub - accept request body and create
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));
        BookReview review = new BookReview();
        review.setBook(book);
        review.setRating(1);
        review.setReviewDate(LocalDateTime.now());
        return bookReviewRepository.save(review);
    }

    public List<BookReview> findByBook(Long bookId) {
        return bookReviewRepository.findByBook_IdAndApproved(bookId, true);
    }

    public BookReview approve(Long reviewId) {
        return approveReview(reviewId);
    }

    public BookReview approveReview(Long reviewId) {
        BookReview review = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + reviewId));
        review.setApproved(true);
        return bookReviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<BookReview> getReviewsForBook(Long bookId) {
        return bookReviewRepository.findByBook_IdAndApproved(bookId, true);
    }

    @Transactional(readOnly = true)
    public List<BookReview> getAllReviewsForBook(Long bookId) {
        return bookReviewRepository.findAll().stream()
                .filter(r -> r.getBook() != null && r.getBook().getId().equals(bookId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long bookId) {
        return bookReviewRepository.averageRatingByBook(bookId);
    }
}
