package com.example.library.repository;

import com.example.library.entity.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    List<BookCopy> findByBook_IdAndStatus(Long bookId, String status);

    List<BookCopy> findByBranch_IdAndStatus(Long branchId, String status);

    Optional<BookCopy> findByBarcode(String barcode);

    long countByBook_IdAndStatus(Long bookId, String status);

    List<BookCopy> findByBook_Id(Long bookId);

    List<BookCopy> findByBranch_Id(Long branchId);

    List<BookCopy> findByStatus(String status);
}
