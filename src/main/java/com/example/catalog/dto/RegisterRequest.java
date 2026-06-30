package com.example.catalog.dto;

import com.example.catalog.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank
    private String firstname;
    private String lastname;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
    private Role role;
}
