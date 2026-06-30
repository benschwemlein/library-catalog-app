package com.example.library.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "book_copy")
@SQLDelete(sql = "UPDATE book_copy SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private LibraryBranch branch;

    @Column(nullable = false, unique = true, length = 50)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CopyCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CopyStatus status;

    @Column(name = "acquired_date")
    private LocalDate acquiredDate;

    @JsonIgnore
    @OneToMany(mappedBy = "bookCopy", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Loan> loans = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
