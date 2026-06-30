package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffMemberDTO {

    private Long id;
    private Long userId;
    private String username;
    private Long branchId;
    private String branchName;
    private String role;
    private LocalDate startDate;
}
