package com.example.library.wishlist;

import com.example.library.entity.Book;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "reading_list_item",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"list_id", "book_id"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ReadingList readingList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Book book;

    @Column(nullable = false)
    private LocalDate addedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ItemPriority priority = ItemPriority.MEDIUM;

    @Column
    private String notes;

    @Builder.Default
    private boolean read = false;

    private LocalDate readDate;

    @Builder.Default
    private int sortOrder = 0;
}
