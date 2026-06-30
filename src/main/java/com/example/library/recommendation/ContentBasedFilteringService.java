package com.example.library.recommendation;

import com.example.library.entity.*;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentBasedFilteringService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public MemberBorrowingProfile buildProfile(Member member) {
        log.info("Building borrowing profile for memberId={}", member.getId());

        List<Loan> activeLoans = loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE);
        List<Loan> returnedLoans = loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.RETURNED);

        List<Loan> allRelevantLoans = new ArrayList<>();
        allRelevantLoans.addAll(activeLoans);
        allRelevantLoans.addAll(returnedLoans);

        Map<String, Integer> genreFrequency = new HashMap<>();
        Map<Long, Integer> authorFrequency = new HashMap<>();
        Set<Long> borrowedBookIds = new HashSet<>();

        for (Loan loan : allRelevantLoans) {
            Book book = loan.getBookCopy().getBook();
            borrowedBookIds.add(book.getId());

            for (Genre genre : book.getGenres()) {
                genreFrequency.merge(genre.getName(), 1, Integer::sum);
            }

            for (Author author : book.getAuthors()) {
                authorFrequency.merge(author.getId(), 1, Integer::sum);
            }
        }

        log.debug("Profile for memberId={}: {} genres, {} authors, {} books borrowed",
                member.getId(), genreFrequency.size(), authorFrequency.size(), borrowedBookIds.size());

        return MemberBorrowingProfile.builder()
                .memberId(member.getId())
                .genreFrequency(genreFrequency)
                .authorFrequency(authorFrequency)
                .borrowedBookIds(borrowedBookIds)
                .builtAt(LocalDateTime.now())
                .build();
    }

    public List<Book> recommend(MemberBorrowingProfile profile, int limit) {
        log.info("Generating content-based recommendations for memberId={}, limit={}", profile.getMemberId(), limit);

        // Find top 3 genres by frequency
        List<String> topGenres = profile.getGenreFrequency().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        log.debug("Top genres for memberId={}: {}", profile.getMemberId(), topGenres);

        Set<Long> knownAuthorIds = profile.getAuthorFrequency().keySet();
        Set<Long> alreadyBorrowed = profile.getBorrowedBookIds();

        List<Book> allBooks = bookRepository.findAll();

        // Score each book not yet borrowed
        Map<Book, Double> scoreMap = new HashMap<>();
        for (Book book : allBooks) {
            if (alreadyBorrowed.contains(book.getId())) {
                continue;
            }

            double primaryScore = 0.0;

            // +3 per matching top genre
            for (Genre genre : book.getGenres()) {
                if (topGenres.contains(genre.getName())) {
                    primaryScore += 3.0;
                }
            }

            // +2 if any author matches profile's known authors
            for (Author author : book.getAuthors()) {
                if (knownAuthorIds.contains(author.getId())) {
                    primaryScore += 2.0;
                    break; // only add the bonus once per book
                }
            }

            if (primaryScore > 0) {
                // Recency tiebreaker only for books with a real match
                double score = primaryScore + 0.0001 * book.getPublicationYear();
                scoreMap.put(book, score);
            }
        }

        List<Book> recommendations = scoreMap.entrySet().stream()
                .sorted(Map.Entry.<Book, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        log.info("Content-based filtering produced {} recommendations for memberId={}", recommendations.size(), profile.getMemberId());
        return recommendations;
    }
}
