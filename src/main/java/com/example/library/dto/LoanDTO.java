package com.example.library.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {

    private Long id;
    private Long bookCopyId;
    private String bookTitle;
    private String bookIsbn;
    private Long memberId;
    private String memberName;
    private String branchName;
    private LocalDateTime checkoutDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private int renewalCount;
    private String status;
    private int daysOverdue;
}
