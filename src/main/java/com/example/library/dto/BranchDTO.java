package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchDTO {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String phone;
    private String email;
    private String openingHours;
    private int totalCopies;
    private int availableCopies;
}
