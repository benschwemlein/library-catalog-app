package com.example.library.digitalresource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DigitalResourceRepository extends JpaRepository<DigitalResource, Long> {

    Page<DigitalResource> findByActiveTrue(Pageable pageable);

    List<DigitalResource> findByResourceTypeAndActiveTrue(DigitalResourceType type);

    Page<DigitalResource> findByTitleContainingIgnoreCaseAndActiveTrue(String title, Pageable pageable);

    List<DigitalResource> findByLicenseType(LicenseType licenseType);
}
