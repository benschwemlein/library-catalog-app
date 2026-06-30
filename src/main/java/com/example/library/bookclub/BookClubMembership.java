package com.example.library.bookclub;

import com.example.library.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "book_club_membership", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"club_id", "member_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookClubMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false)
    private BookClub club;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate joinDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookClubMemberRole role = BookClubMemberRole.MEMBER;

    @Builder.Default
    private boolean active = true;
}
