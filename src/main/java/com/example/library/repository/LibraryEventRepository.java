package com.example.library.repository;

import com.example.library.entity.LibraryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LibraryEventRepository extends JpaRepository<LibraryEvent, Long> {

    List<LibraryEvent> findByBranch_Id(Long branchId);

    @Query("SELECT e FROM LibraryEvent e WHERE e.startDateTime > :now ORDER BY e.startDateTime ASC")
    List<LibraryEvent> findUpcomingEvents(@Param("now") LocalDateTime now);

    List<LibraryEvent> findByBranch_IdAndStartDateTimeAfter(Long branchId, LocalDateTime after);

    List<LibraryEvent> findByEventType(String eventType);

    List<LibraryEvent> findByStartDateTimeAfterOrderByStartDateTimeAsc(LocalDateTime after);

    List<LibraryEvent> findByBranchIdOrderByStartDateTimeAsc(Long branchId);
}
