package com.example.library.controller;

import com.example.library.dto.CreateReviewRequest;
import com.example.library.entity.BookReview;
import com.example.library.service.BookReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class BookReviewController {

    @Autowired
    private BookReviewService bookReviewService;

    @PostMapping("/books/{bookId}/reviews")
    public ResponseEntity<BookReview> createReview(@PathVariable Long bookId,
                                                   @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookReviewService.submitReview(bookId, request.getMemberId(), request));
    }

    @GetMapping("/books/{bookId}/reviews")
    public ResponseEntity<List<BookReview>> getReviewsByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(bookReviewService.findByBook(bookId));
    }

    @PutMapping("/reviews/{id}/approve")
    public ResponseEntity<BookReview> approveReview(@PathVariable Long id) {
        return ResponseEntity.ok(bookReviewService.approve(id));
    }
}
