package com.example.library.interlibrary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterLibraryLoanDTO {

    private Long id;
    private Long memberId;
    private String memberName;
    private Long branchId;
    private String branchName;
    private String bookTitle;
    private String authorName;
    private String isbn;
    private LocalDate requestDate;
    private LocalDate neededByDate;
    private ILLStatus status;
    private Long partnerLibraryId;
    private String partnerLibraryName;
    private String notes;
    private LocalDate estimatedArrival;
    private String reviewedByName;
    private String reviewNote;
    private String denialReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
