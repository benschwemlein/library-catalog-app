package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {

    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String membershipNumber;
    private String membershipTier;
    private LocalDate joinDate;
    private LocalDate expiryDate;
    private BigDecimal fineBalance;
    private boolean active;
    private int currentLoans;
    private int activeHolds;
}
