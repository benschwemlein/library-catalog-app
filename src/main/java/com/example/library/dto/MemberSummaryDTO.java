package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSummaryDTO {

    private Long id;
    private String membershipNumber;
    private String fullName;
    private String membershipTier;
    private boolean active;
    private BigDecimal fineBalance;
}
