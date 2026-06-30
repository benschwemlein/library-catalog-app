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
public class HoldDTO {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private Long memberId;
    private String memberName;
    private LocalDateTime requestDate;
    private LocalDateTime expiryDate;
    private String status;
    private String pickupBranchName;
    private LocalDateTime notifiedDate;
}
