package com.example.library.dto;

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
public class FineDTO {

    private Long id;
    private Long loanId;
    private Long memberId;
    private String memberName;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime issuedDate;
    private LocalDateTime paidDate;
    private boolean waived;
    private String waivedBy;
    private String waivedReason;
    private String bookTitle;
}
