package com.example.library.bookclub;

import com.example.library.entity.Book;
import com.example.library.entity.LibraryBranch;
import com.example.library.entity.StaffMember;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_club")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookClub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private LibraryBranch branch;

    @ManyToOne
    @JoinColumn(name = "facilitator_id")
    private StaffMember facilitator;

    @Builder.Default
    private int maxMembers = 20;

    @Column(length = 500)
    private String meetingSchedule;

    @ManyToOne
    @JoinColumn(name = "current_book_id")
    private Book currentBook;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookClubStatus status = BookClubStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
