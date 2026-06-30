package com.example.library.periodical;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "periodical_issue")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicalIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periodical_id", nullable = false)
    @ToString.Exclude
    private Periodical periodical;

    private int volume;

    private int issueNumber;

    @Column(nullable = false)
    private LocalDate publicationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PeriodicalIssueStatus status = PeriodicalIssueStatus.CURRENT;

    @Column(length = 50)
    private String condition;

    @Column(length = 200)
    private String location;

    private LocalDate receivedDate;

    @Column
    private String notes;
}
