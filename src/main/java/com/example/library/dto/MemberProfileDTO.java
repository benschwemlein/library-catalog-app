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
public class MemberProfileDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String membershipNumber;
    private String membershipTier;
    private LocalDate joinDate;
    private LocalDate expiryDate;
}
