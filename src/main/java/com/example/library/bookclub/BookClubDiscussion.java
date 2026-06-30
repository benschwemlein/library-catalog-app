package com.example.library.bookclub;

import com.example.library.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_club_discussion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookClubDiscussion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false)
    private BookClub club;

    @ManyToOne
    @JoinColumn(name = "meeting_id")
    private BookClubMeeting meeting;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member poster;

    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime postedAt;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private BookClubDiscussion parentDiscussion;

    @Builder.Default
    private boolean edited = false;

    private LocalDateTime editedAt;
}
