package com.example.library.periodical;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PeriodicalSubscriptionRepository extends JpaRepository<PeriodicalSubscription, Long> {

    List<PeriodicalSubscription> findByBranchIdAndActiveTrue(Long branchId);

    List<PeriodicalSubscription> findByRenewalDateBeforeAndActiveTrue(LocalDate date);
}
