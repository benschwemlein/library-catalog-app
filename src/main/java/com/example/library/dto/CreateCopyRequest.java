package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCopyRequest {

    @NotNull
    private Long bookId;

    @NotNull
    private Long branchId;

    @NotBlank
    private String barcode;

    @Builder.Default
    private String condition = "GOOD";
}
