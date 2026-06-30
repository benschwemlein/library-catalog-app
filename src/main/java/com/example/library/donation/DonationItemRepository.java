package com.example.library.donation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationItemRepository extends JpaRepository<DonationItem, Long> {
    List<DonationItem> findByDonationId(Long donationId);
    long countByDisposition(ItemDisposition disposition);
}
