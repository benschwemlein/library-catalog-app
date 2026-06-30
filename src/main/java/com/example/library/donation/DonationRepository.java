package com.example.library.donation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    Page<Donation> findByStatus(DonationStatus status, Pageable pageable);
    List<Donation> findByDonorDonorEmail(String email);
    long countByStatus(DonationStatus status);
}
