package com.example.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "book_review")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int rating;

    @Column(name = "review_text")
    private String reviewText;

    @Column(name = "review_date", nullable = false)
    private LocalDateTime reviewDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean approved = false;
}
