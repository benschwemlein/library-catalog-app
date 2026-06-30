package com.example.library.interlibrary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartnerLibraryRepository extends JpaRepository<PartnerLibrary, Long> {

    List<PartnerLibrary> findByActiveTrue();

    Optional<PartnerLibrary> findByName(String name);
}
