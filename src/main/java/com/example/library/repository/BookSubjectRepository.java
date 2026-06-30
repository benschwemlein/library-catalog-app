package com.example.library.repository;

import com.example.library.entity.BookSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookSubjectRepository extends JpaRepository<BookSubject, Long> {

    Optional<BookSubject> findByNameIgnoreCase(String name);

    List<BookSubject> findByNameContaining(String name);
}
