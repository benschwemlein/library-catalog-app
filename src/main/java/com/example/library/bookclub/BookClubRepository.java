package com.example.library.bookclub;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookClubRepository extends JpaRepository<BookClub, Long> {
    List<BookClub> findByBranchId(Long branchId);
    List<BookClub> findByStatus(BookClubStatus status);
    List<BookClub> findByNameContainingIgnoreCase(String name);
    List<BookClub> findByFacilitatorId(Long facilitatorId);
}
