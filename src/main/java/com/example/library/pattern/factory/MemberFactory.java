package com.example.library.pattern.factory;

import com.example.catalog.model.User;
import com.example.library.entity.Member;
import com.example.library.entity.MembershipTier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Factory for constructing {@link Member} entities.
 *
 * <p>All methods return a new, unsaved {@link Member}. The caller is responsible
 * for persisting the member via {@code MemberRepository.save()}.</p>
 */
@Component
@Slf4j
public class MemberFactory {

    private static final int STANDARD_MEMBERSHIP_DAYS = 365;
    private static final int STUDENT_MEMBERSHIP_DAYS = 180;
    private static final int PREMIUM_MEMBERSHIP_DAYS = 365;

    /**
     * Create a STANDARD-tier member account for the given user.
     * Membership is valid for 365 days from today.
     *
     * @param user the platform user to associate with the new member account
     * @return an unsaved Member entity
     */
    public Member createStandardMember(User user) {
        LocalDate today = LocalDate.now();
        String membershipNumber = generateMembershipNumber("STD");

        Member member = Member.builder()
                .user(user)
                .membershipNumber(membershipNumber)
                .membershipTier(MembershipTier.STANDARD)
                .joinDate(today)
                .expiryDate(today.plusDays(STANDARD_MEMBERSHIP_DAYS))
                .fineBalance(BigDecimal.ZERO)
                .active(true)
                .build();

        log.info("Created STANDARD member: membershipNumber={} user={} expiry={}",
                membershipNumber, user.getEmail(), member.getExpiryDate());
        return member;
    }

    /**
     * Create a STUDENT-tier member account for the given user.
     * Student memberships are valid for 180 days (roughly one academic year semester).
     * The institution name and student ID are logged for verification purposes.
     *
     * @param user        the platform user to associate with the new member account
     * @param institution the academic institution the student attends
     * @param studentId   the student's institutional ID number
     * @return an unsaved Member entity
     */
    public Member createStudentMember(User user, String institution, String studentId) {
        LocalDate today = LocalDate.now();
        String membershipNumber = generateMembershipNumber("STU");

        Member member = Member.builder()
                .user(user)
                .membershipNumber(membershipNumber)
                .membershipTier(MembershipTier.STUDENT)
                .joinDate(today)
                .expiryDate(today.plusDays(STUDENT_MEMBERSHIP_DAYS))
                .fineBalance(BigDecimal.ZERO)
                .active(true)
                .build();

        log.info("Created STUDENT member: membershipNumber={} user={} institution='{}' studentId='{}' expiry={}",
                membershipNumber, user.getEmail(), institution, studentId, member.getExpiryDate());
        return member;
    }

    /**
     * Create a PREMIUM-tier member account for the given user.
     * Premium members receive extended loan periods, more renewals, and lower daily fine rates.
     *
     * @param user the platform user to associate with the new member account
     * @return an unsaved Member entity
     */
    public Member createPremiumMember(User user) {
        LocalDate today = LocalDate.now();
        String membershipNumber = generateMembershipNumber("PRM");

        Member member = Member.builder()
                .user(user)
                .membershipNumber(membershipNumber)
                .membershipTier(MembershipTier.PREMIUM)
                .joinDate(today)
                .expiryDate(today.plusDays(PREMIUM_MEMBERSHIP_DAYS))
                .fineBalance(BigDecimal.ZERO)
                .active(true)
                .build();

        log.info("Created PREMIUM member: membershipNumber={} user={} expiry={}",
                membershipNumber, user.getEmail(), member.getExpiryDate());
        return member;
    }

    /**
     * Generate a unique membership number.
     * Format: {PREFIX}-{6-digit UUID segment}
     * Example: STD-A3F8C2
     */
    private String generateMembershipNumber(String prefix) {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return prefix + "-" + token;
    }
}
