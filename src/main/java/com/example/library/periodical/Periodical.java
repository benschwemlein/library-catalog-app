package com.example.library.periodical;

import com.example.library.entity.LibraryBranch;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "periodical")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Periodical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 20, unique = true)
    private String issn;

    private String publisher;

    @Enumerated(EnumType.STRING)
    private PeriodicalFrequency frequency;

    @Column(length = 100)
    private String category;

    @Column
    private String description;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean digitalAccess = false;

    @Column(length = 2000)
    private String digitalUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private LibraryBranch branch;

    @OneToMany(mappedBy = "periodical", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<PeriodicalIssue> issues = new ArrayList<>();

    @OneToMany(mappedBy = "periodical", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<PeriodicalSubscription> subscriptions = new ArrayList<>();
}
