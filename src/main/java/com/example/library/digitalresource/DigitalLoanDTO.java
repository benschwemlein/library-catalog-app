package com.example.library.digitalresource;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalLoanDTO {

    private Long loanId;
    private Long resourceId;
    private String resourceTitle;
    private Long memberId;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private int downloadCount;
    private DigitalLoanStatus status;
    private String fileUrl; // only populated when status is ACTIVE
}
