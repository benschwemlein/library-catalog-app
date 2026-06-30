package com.example.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchStatsDTO {

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotBlank(message = "Branch name is required")
    private String branchName;

    @Min(value = 0, message = "Total copies cannot be negative")
    private int totalCopies;

    @Min(value = 0, message = "Available copies cannot be negative")
    private int availableCopies;

    @Min(value = 0, message = "Checked out copies cannot be negative")
    private int checkedOutCopies;

    @Min(value = 0, message = "Active loans cannot be negative")
    private int activeLoans;

    @Min(value = 0, message = "Overdue loans cannot be negative")
    private int overdueLoans;

    @Min(value = 0, message = "Total members cannot be negative")
    private int totalMembers;
}
