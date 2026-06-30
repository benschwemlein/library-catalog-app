package com.example.library.bookclub;

import com.example.library.entity.Book;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_club_meeting")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookClubMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false)
    private BookClub club;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book discussedBook;

    @Column(nullable = false)
    private LocalDateTime meetingDate;

    @Column(length = 500)
    private String location;

    @Column
    private String notes;

    @Builder.Default
    private int attendanceCount = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
