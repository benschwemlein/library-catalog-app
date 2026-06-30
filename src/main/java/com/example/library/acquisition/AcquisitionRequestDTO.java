package com.example.library.acquisition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcquisitionRequestDTO {

    private Long id;
    private Long memberId;
    private String memberName;
    private String requestedByStaff;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private String reason;
    private AcquisitionPriority priority;
    private AcquisitionStatus status;
    private LocalDate requestDate;
    private String reviewedByName;
    private String reviewNote;
    private BigDecimal estimatedCost;
    private Long targetBranchId;
    private String targetBranchName;
    private Long purchaseOrderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
