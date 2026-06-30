package com.example.library.repository;

import com.example.library.entity.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthors_LastNameIgnoreCase(String lastName);

    List<Book> findByGenres_Name(String genreName);

    List<Book> findByPublicationYearBetween(int startYear, int endYear);

    List<Book> findByLanguage(String language);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(b.description) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Book> searchByTitleOrDescription(@Param("q") String query);

    @Query("SELECT b FROM Book b JOIN b.copies c WHERE c.status = 'AVAILABLE' GROUP BY b HAVING COUNT(c) > 0")
    List<Book> findBooksWithAvailableCopies();

    @Query("SELECT b FROM Book b ORDER BY b.createdAt DESC")
    List<Book> findRecentlyAdded(Pageable pageable);

    boolean existsByIsbn(String isbn);
}
