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
public class LoanSummaryDTO {

    private Long id;
    private String bookTitle;
    private LocalDateTime dueDate;
    private String status;
    private int renewalCount;
    private int daysOverdue;
}
