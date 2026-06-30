package com.example.library.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberActivityReportDTO {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotBlank(message = "Member name is required")
    private String memberName;

    @NotBlank(message = "Membership number is required")
    private String membershipNumber;

    @Min(value = 0, message = "Total loans cannot be negative")
    private int totalLoans;

    @Min(value = 0, message = "Current loans cannot be negative")
    private int currentLoans;

    @Min(value = 0, message = "Overdue count cannot be negative")
    private int overdueCount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Total fines cannot be negative")
    private BigDecimal totalFines;

    @DecimalMin(value = "0.0", inclusive = true, message = "Unpaid fines cannot be negative")
    private BigDecimal unpaidFines;

    @Min(value = 0, message = "Holds cannot be negative")
    private int holds;
}
