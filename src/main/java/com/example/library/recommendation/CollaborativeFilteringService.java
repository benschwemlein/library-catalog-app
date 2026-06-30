package com.example.library.recommendation;

import com.example.library.entity.Book;
import com.example.library.entity.Loan;
import com.example.library.entity.Member;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollaborativeFilteringService {

    private static final int MIN_OVERLAP = 2;

    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    public List<Member> findSimilarMembers(Member member, int limit) {
        log.info("Finding similar members for memberId={}, limit={}", member.getId(), limit);

        Set<Long> myBookIds = loanRepository.findByMember_Id(member.getId()).stream()
                .map(loan -> loan.getBookCopy().getBook().getId())
                .collect(Collectors.toSet());

        if (myBookIds.isEmpty()) {
            log.debug("memberId={} has no loan history; no similar members possible", member.getId());
            return Collections.emptyList();
        }

        List<Member> allOtherMembers = memberRepository.findAll().stream()
                .filter(m -> !m.getId().equals(member.getId()))
                .collect(Collectors.toList());

        Map<Member, Long> overlapCounts = new HashMap<>();
        for (Member other : allOtherMembers) {
            Set<Long> theirBookIds = loanRepository.findByMember_Id(other.getId()).stream()
                    .map(loan -> loan.getBookCopy().getBook().getId())
                    .collect(Collectors.toSet());

            Set<Long> intersection = new HashSet<>(myBookIds);
            intersection.retainAll(theirBookIds);

            if (intersection.size() >= MIN_OVERLAP) {
                overlapCounts.put(other, (long) intersection.size());
            }
        }

        List<Member> similarMembers = overlapCounts.entrySet().stream()
                .sorted(Map.Entry.<Member, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        log.info("Found {} similar members for memberId={}", similarMembers.size(), member.getId());
        return similarMembers;
    }

    public List<Book> recommend(Member member, List<Member> similarMembers, Set<Long> alreadyBorrowed, int limit) {
        log.info("Generating collaborative recommendations for memberId={} using {} similar members, limit={}",
                member.getId(), similarMembers.size(), limit);

        if (similarMembers.isEmpty()) {
            return Collections.emptyList();
        }

        // bookId -> how many similar members borrowed it
        Map<Long, Integer> bookScores = new HashMap<>();
        for (Member similarMember : similarMembers) {
            List<Loan> loans = loanRepository.findByMember_Id(similarMember.getId());
            for (Loan loan : loans) {
                Long bookId = loan.getBookCopy().getBook().getId();
                if (!alreadyBorrowed.contains(bookId)) {
                    bookScores.merge(bookId, 1, Integer::sum);
                }
            }
        }

        // Sort by score desc and take top candidates
        List<Long> topBookIds = bookScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Load full Book objects, preserving order
        Map<Long, Book> bookById = new HashMap<>();
        for (Long bookId : topBookIds) {
            bookRepository.findById(bookId).ifPresent(b -> bookById.put(b.getId(), b));
        }

        List<Book> recommendations = topBookIds.stream()
                .map(bookById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Collaborative filtering produced {} recommendations for memberId={}", recommendations.size(), member.getId());
        return recommendations;
    }
}
