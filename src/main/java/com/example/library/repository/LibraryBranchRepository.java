package com.example.library.repository;

import com.example.library.entity.LibraryBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibraryBranchRepository extends JpaRepository<LibraryBranch, Long> {

    List<LibraryBranch> findByCity(String city);

    List<LibraryBranch> findByNameContainingIgnoreCase(String name);

    List<LibraryBranch> findByActiveTrue();
}
