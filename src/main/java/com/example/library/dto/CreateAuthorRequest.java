package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuthorRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String bio;
    private LocalDate birthDate;
    private String nationality;
}
