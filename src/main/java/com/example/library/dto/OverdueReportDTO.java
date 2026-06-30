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
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueReportDTO {

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    @NotBlank(message = "Book title is required")
    private String bookTitle;

    @NotBlank(message = "Member name is required")
    private String memberName;

    @NotBlank(message = "Membership number is required")
    private String membershipNumber;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    @Min(value = 0, message = "Days overdue cannot be negative")
    private int daysOverdue;

    @DecimalMin(value = "0.0", inclusive = true, message = "Fine amount cannot be negative")
    private BigDecimal fineAmount;

    private String branchName;
}
