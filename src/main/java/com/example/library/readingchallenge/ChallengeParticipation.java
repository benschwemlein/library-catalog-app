package com.example.library.readingchallenge;

import com.example.library.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "challenge_participation",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"challenge_id", "member_id"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private ReadingChallenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate enrollDate;

    @Builder.Default
    private int completedBooks = 0;

    private LocalDate completedDate;

    @Builder.Default
    private boolean badgeEarned = false;
}
