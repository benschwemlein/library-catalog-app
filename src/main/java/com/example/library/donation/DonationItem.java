package com.example.library.donation;

import com.example.library.entity.Book;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "donation_item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id", nullable = false)
    @ToString.Exclude
    private Donation donation;

    @Column(nullable = false)
    private String title;

    private String author;

    @Column(length = 20)
    private String isbn;

    @Builder.Default
    private int quantity = 1;

    @Column(length = 50)
    private String condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ItemDisposition disposition = ItemDisposition.PENDING;

    @Column
    private String dispositionNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    @ToString.Exclude
    private Book addedBook;
}
