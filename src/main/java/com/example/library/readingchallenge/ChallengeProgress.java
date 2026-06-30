package com.example.library.readingchallenge;

import com.example.library.entity.Book;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "challenge_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id", nullable = false)
    private ChallengeParticipation participation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(length = 500)
    private String bookTitle;

    @Column(nullable = false)
    private LocalDate completedDate;

    @Column
    private String notes;

    @Builder.Default
    private boolean verified = false;
}
