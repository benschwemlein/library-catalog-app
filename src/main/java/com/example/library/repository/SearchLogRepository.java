package com.example.library.repository;

import com.example.library.entity.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    List<SearchLog> findByUserId(Long userId);

    List<SearchLog> findBySessionId(String sessionId);

    List<SearchLog> findByQueryContaining(String query);

    @Query("SELECT s.query, COUNT(s) as cnt FROM SearchLog s GROUP BY s.query ORDER BY cnt DESC")
    List<Object[]> findTopSearchQueries();
}
