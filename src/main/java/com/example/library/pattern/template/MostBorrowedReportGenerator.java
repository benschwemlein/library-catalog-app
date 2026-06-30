package com.example.library.pattern.template;

import com.example.library.entity.Book;
import com.example.library.entity.Loan;
import com.example.library.pattern.builder.ReportCriteria;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MostBorrowedReportGenerator extends AbstractReportGenerator<Book> {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    private final Map<Long, Long> bookLoanCounts = new HashMap<>();

    @Override
    protected void validateCriteria(ReportCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("ReportCriteria cannot be null");
        }
    }

    @Override
    protected List<Book> fetchData(ReportCriteria criteria) {
        bookLoanCounts.clear();
        int maxResults = criteria.getMaxResults() > 0 ? criteria.getMaxResults() : 10;

        List<Loan> loans = loanRepository.findAll().stream()
            .filter(loan -> {
                if (criteria.getDateFrom() != null && loan.getCheckoutDate() != null
                        && loan.getCheckoutDate().toLocalDate().isBefore(criteria.getDateFrom())) {
                    return false;
                }
                if (criteria.getDateTo() != null && loan.getCheckoutDate() != null
                        && loan.getCheckoutDate().toLocalDate().isAfter(criteria.getDateTo())) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());

        Map<Book, Long> countsByBook = loans.stream()
            .filter(loan -> loan.getBookCopy() != null && loan.getBookCopy().getBook() != null)
            .collect(Collectors.groupingBy(
                loan -> loan.getBookCopy().getBook(),
                Collectors.counting()
            ));

        countsByBook.forEach((book, count) -> bookLoanCounts.put(book.getId(), count));

        return countsByBook.entrySet().stream()
            .sorted(Map.Entry.<Book, Long>comparingByValue(Comparator.reverseOrder()))
            .limit(maxResults)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    @Override
    protected ReportData processData(List<Book> data) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            Book book = data.get(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", i + 1);
            row.put("bookId", book.getId());
            row.put("title", book.getTitle());
            row.put("isbn", book.getIsbn());
            row.put("loanCount", bookLoanCounts.getOrDefault(book.getId(), 0L));
            rows.add(row);
        }

        Map<String, Object> aggregates = new HashMap<>();
        aggregates.put("totalBooksRanked", data.size());
        aggregates.put("topBook", data.isEmpty() ? "N/A" : data.get(0).getTitle());

        return new ReportData(new HashMap<>(), rows, aggregates);
    }

    @Override
    protected Report formatReport(ReportData reportData, ReportCriteria criteria) {
        Map<String, Object> aggregates = reportData.getAggregates();

        ReportSection rankingSection = new ReportSection(
            "Most Borrowed Books",
            reportData.getRows(),
            "Top " + reportData.getRows().size() + " most borrowed books"
        );

        List<ReportSection> sections = new ArrayList<>();
        sections.add(rankingSection);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reportType", "MOST_BORROWED");
        metadata.put("topBook", aggregates.getOrDefault("topBook", "N/A"));
        metadata.put("generatedBy", "MostBorrowedReportGenerator");

        return new Report("Most Borrowed Books Report", LocalDateTime.now(), criteria, sections, metadata);
    }
}
