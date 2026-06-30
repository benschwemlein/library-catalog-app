package com.example.library.digitalresource;

import com.example.library.entity.LibraryBranch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DigitalLicenseRepository extends JpaRepository<DigitalLicense, Long> {

    List<DigitalLicense> findByResourceAndActiveTrue(DigitalResource resource);

    Optional<DigitalLicense> findByResourceAndBranchAndActiveTrue(DigitalResource resource, LibraryBranch branch);
}
