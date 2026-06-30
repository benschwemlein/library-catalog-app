package com.example.library.circulation;

import com.example.library.entity.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CirculationRuleRepository extends JpaRepository<CirculationRule, Long> {

    List<CirculationRule> findByMembershipTierAndItemTypeAndActiveTrue(MembershipTier tier, ItemType itemType);

    List<CirculationRule> findByBranchIdAndActiveTrue(Long branchId);

    List<CirculationRule> findByItemTypeAndActiveTrueAndMembershipTierIsNull(ItemType itemType);

    List<CirculationRule> findByActiveTrue();

    /**
     * Finds applicable rules ordered by specificity: branch-specific before global,
     * tier-specific before generic. The most specific rule is returned first.
     */
    @Query("SELECT r FROM CirculationRule r WHERE r.active = true " +
           "AND (r.membershipTier = :tier OR r.membershipTier IS NULL) " +
           "AND r.itemType = :itemType " +
           "AND (r.branchId = :branchId OR r.branchId IS NULL) " +
           "ORDER BY CASE WHEN r.branchId IS NOT NULL THEN 0 ELSE 1 END, " +
           "CASE WHEN r.membershipTier IS NOT NULL THEN 0 ELSE 1 END")
    List<CirculationRule> findApplicableRules(@Param("tier") MembershipTier tier,
                                              @Param("itemType") ItemType itemType,
                                              @Param("branchId") Long branchId);
}
