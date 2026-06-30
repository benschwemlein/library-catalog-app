package com.example.library.batch;

import com.example.library.entity.Member;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * GDPR member cleanup: anonymizes inactive members who have been deactivated
 * for at least 90 days and have no outstanding active loans.
 *
 * Anonymization replaces PII fields on the linked User entity and the Member
 * itself. Because the User entity is a catalog-module concern, we rely on
 * the Member -> User relationship already loaded via the Member entity.
 *
 * Note: There is no token repository wired into the library module; any
 * session/refresh tokens for deactivated users should be revoked by the
 * catalog authentication module on deactivation. We log a reminder here.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberCleanupBatchProcessor {

    /** Members deactivated at least this many days ago are eligible for anonymization. */
    private static final int INACTIVITY_DAYS_THRESHOLD = 90;

    private final BatchJobRepository batchJobRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    @Transactional
    public void process(BatchJob job) {
        log.info("Starting MEMBER_CLEANUP job id={}", job.getId());
        log.info("Note: auth token cleanup for deactivated users is handled by the catalog auth module "
                + "at deactivation time and is not repeated here.");

        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        batchJobRepository.save(job);

        int processed = 0;
        int failed = 0;
        int skipped = 0;

        try {
            // MemberRepository does not expose findByActiveFalse, so we fetch all and filter
            List<Member> allMembers = memberRepository.findAll();
            LocalDate cutoff = LocalDate.now().minusDays(INACTIVITY_DAYS_THRESHOLD);

            for (Member member : allMembers) {
                if (member.isActive()) {
                    continue;
                }

                // Use joinDate as a conservative lower bound for "how long inactive" —
                // members who never renewed and whose expiry date is before the cutoff
                // are eligible. Using expiryDate as the deactivation proxy is safer than
                // joinDate and is available on every Member.
                if (member.getExpiryDate() == null || member.getExpiryDate().isAfter(cutoff)) {
                    skipped++;
                    continue;
                }

                // Skip members with open active loans (data integrity guard)
                boolean hasActiveLoans = !loanRepository.findByMember_Id(member.getId()).isEmpty()
                        && loanRepository.findByMember_Id(member.getId()).stream()
                                .anyMatch(l -> l.getStatus().name().equals("ACTIVE")
                                        || l.getStatus().name().equals("OVERDUE"));
                if (hasActiveLoans) {
                    log.warn("Skipping anonymization of inactive member id={}: has outstanding loans", member.getId());
                    skipped++;
                    continue;
                }

                // Skip if already anonymized
                if (member.getMembershipNumber() != null
                        && member.getMembershipNumber().startsWith("DELETED-")) {
                    skipped++;
                    continue;
                }

                try {
                    anonymizeMember(member);
                    processed++;
                } catch (Exception e) {
                    log.error("Failed to anonymize member id={}: {}", member.getId(), e.getMessage(), e);
                    failed++;
                }

                if ((processed + failed) % 50 == 0) {
                    job.setRecordsProcessed(processed);
                    job.setRecordsFailed(failed);
                    batchJobRepository.save(job);
                }
            }

            job.setStatus(failed > 0 && processed == 0 ? JobStatus.FAILED : JobStatus.COMPLETED);
            log.info("MEMBER_CLEANUP job id={} completed: anonymized={} failed={} skipped={}",
                    job.getId(), processed, failed, skipped);

        } catch (Exception e) {
            log.error("MEMBER_CLEANUP job id={} encountered a fatal error: {}", job.getId(), e.getMessage(), e);
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
        }

        job.setRecordsProcessed(processed);
        job.setRecordsFailed(failed);
        job.setCompletedAt(LocalDateTime.now());
        batchJobRepository.save(job);
    }

    private void anonymizeMember(Member member) {
        log.info("Anonymizing member id={} membershipNumber={}", member.getId(), member.getMembershipNumber());

        // Anonymize membership number so the row is still identifiable as deleted
        member.setMembershipNumber("DELETED-" + member.getId());

        // Anonymize the linked catalog User entity (PII fields: email, firstName, lastName)
        if (member.getUser() != null) {
            com.example.catalog.model.User user = member.getUser();
            user.setEmail("anonymized_" + member.getId() + "@deleted.local");
            user.setFirstName("Deleted");
            user.setLastName("User");
            // The User entity is in the JPA context via the Member association and will
            // be flushed by the enclosing @Transactional on process().
        }

        // Fine balance is retained for audit / accounting reconciliation
        memberRepository.save(member);
        log.debug("Member id={} anonymized successfully", member.getId());
    }
}
