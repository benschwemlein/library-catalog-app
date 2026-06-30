package com.example.library.periodical;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PeriodicalIssueRepository extends JpaRepository<PeriodicalIssue, Long> {

    List<PeriodicalIssue> findByPeriodicalId(Long periodicalId);

    List<PeriodicalIssue> findByPeriodicalIdAndStatus(Long periodicalId, PeriodicalIssueStatus status);

    Optional<PeriodicalIssue> findByPeriodicalIdAndVolumeAndIssueNumber(Long periodicalId, int volume, int issueNumber);
}
