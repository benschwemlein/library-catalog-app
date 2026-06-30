package com.example.library.readingchallenge;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReadingChallengeRepository extends JpaRepository<ReadingChallenge, Long> {
    List<ReadingChallenge> findByActiveTrue();
    List<ReadingChallenge> findByActiveTrueAndEndDateAfter(LocalDate date);
}
