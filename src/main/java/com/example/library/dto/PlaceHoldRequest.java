package com.example.library.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceHoldRequest {

    @NotNull
    private Long memberId;

    @NotNull
    private Long bookId;

    @NotNull
    private Long pickupBranchId;
}
