package com.example.library.recommendation;

import com.example.library.entity.*;
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
public class HybridRecommendationService {

    private static final double COLLABORATIVE_WEIGHT = 0.6;
    private static final double CONTENT_WEIGHT = 0.4;

    private final ContentBasedFilteringService contentBasedFilteringService;
    private final CollaborativeFilteringService collaborativeFilteringService;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public List<RecommendationDTO> getRecommendations(Member member, int limit) {
        log.info("Generating hybrid recommendations for memberId={}, limit={}", member.getId(), limit);

        MemberBorrowingProfile profile = contentBasedFilteringService.buildProfile(member);
        Set<Long> alreadyBorrowed = profile.getBorrowedBookIds();

        // Collaborative recommendations
        List<Member> similarMembers = collaborativeFilteringService.findSimilarMembers(member, 10);
        List<Book> collaborativeBooks = collaborativeFilteringService.recommend(member, similarMembers, alreadyBorrowed, limit * 2);

        // Content-based recommendations
        List<Book> contentBooks = contentBasedFilteringService.recommend(profile, limit * 2);

        Set<Long> collaborativeBookIds = collaborativeBooks.stream()
                .map(Book::getId)
                .collect(Collectors.toSet());
        Set<Long> contentBookIds = contentBooks.stream()
                .map(Book::getId)
                .collect(Collectors.toSet());

        // Normalize scores 0..1 for collaborative list (index-based: rank 0 = score 1.0, last = ~0)
        Map<Long, Double> collaborativeScores = normalizeRankScores(collaborativeBooks);
        Map<Long, Double> contentScores = normalizeRankScores(contentBooks);

        // Merge into combined score map
        Set<Long> allBookIds = new HashSet<>();
        allBookIds.addAll(collaborativeBookIds);
        allBookIds.addAll(contentBookIds);

        Map<Long, Double> combinedScores = new HashMap<>();
        for (Long bookId : allBookIds) {
            double collScore = collaborativeScores.getOrDefault(bookId, 0.0);
            double contScore = contentScores.getOrDefault(bookId, 0.0);
            combinedScores.put(bookId, COLLABORATIVE_WEIGHT * collScore + CONTENT_WEIGHT * contScore);
        }

        // Collect all unique books
        Map<Long, Book> bookById = new HashMap<>();
        for (Book b : collaborativeBooks) {
            bookById.put(b.getId(), b);
        }
        for (Book b : contentBooks) {
            bookById.put(b.getId(), b);
        }

        // Sort by combined score and build DTOs
        List<RecommendationDTO> results = combinedScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Long bookId = entry.getKey();
                    double score = entry.getValue();
                    Book book = bookById.get(bookId);
                    if (book == null) {
                        return null;
                    }

                    boolean inCollab = collaborativeBookIds.contains(bookId);
                    boolean inContent = contentBookIds.contains(bookId);

                    String reason;
                    if (inCollab && inContent) {
                        reason = "Highly recommended based on your history and similar readers";
                    } else if (inCollab) {
                        reason = "Popular with members who share your reading taste";
                    } else {
                        reason = "Based on your reading history";
                    }

                    List<String> authorNames = book.getAuthors().stream()
                            .map(a -> a.getFirstName() + " " + a.getLastName())
                            .collect(Collectors.toList());

                    List<String> genreNames = book.getGenres().stream()
                            .map(Genre::getName)
                            .collect(Collectors.toList());

                    boolean available = book.getCopies().stream()
                            .anyMatch(copy -> CopyStatus.AVAILABLE.equals(copy.getStatus()));

                    return RecommendationDTO.builder()
                            .bookId(book.getId())
                            .title(book.getTitle())
                            .isbn(book.getIsbn())
                            .authors(authorNames)
                            .genres(genreNames)
                            .reason(reason)
                            .score(score)
                            .publicationYear(book.getPublicationYear())
                            .available(available)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Hybrid recommendation produced {} results for memberId={}", results.size(), member.getId());
        return results;
    }

    /**
     * Assigns a normalized score based on rank position (index 0 = 1.0, last index = close to 0).
     * If the list has only one item, that item gets score 1.0.
     */
    private Map<Long, Double> normalizeRankScores(List<Book> books) {
        Map<Long, Double> scores = new HashMap<>();
        int size = books.size();
        if (size == 0) {
            return scores;
        }
        for (int i = 0; i < size; i++) {
            double normalized = (size == 1) ? 1.0 : 1.0 - ((double) i / (size - 1));
            scores.put(books.get(i).getId(), normalized);
        }
        return scores;
    }
}
