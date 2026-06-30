package com.example.library.periodical;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PeriodicalRepository extends JpaRepository<Periodical, Long> {

    Page<Periodical> findByActiveTrue(Pageable pageable);

    List<Periodical> findByCategory(String category);

    Page<Periodical> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    List<Periodical> findByBranchId(Long branchId);

    @Query("SELECT DISTINCT p.category FROM Periodical p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findDistinctCategories();
}
