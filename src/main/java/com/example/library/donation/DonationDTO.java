package com.example.library.donation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationDTO {

    private Long id;
    private String donorName;
    private String donorEmail;
    private String donorPhone;
    private String donorAddress;
    private LocalDate donationDate;
    private DonationStatus status;
    private String reviewedByName;
    private String notes;
    private boolean acknowledgementSent;
    private LocalDate acknowledgementDate;
    private Long targetBranchId;
    private String targetBranchName;
    private List<DonationItemDTO> items;
    private int itemCount;
    private LocalDateTime createdAt;
}
