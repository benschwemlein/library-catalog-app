package com.example.catalog.dto;

import java.util.List;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CatalogItemRequestDTO {

    private Long id;

    @Nonnull
    private String title;

    @Nonnull
    private String description;

    @Nonnull
    private UserRequestDTO createdBy;

    private List<CatalogIdDTO> catalogIds;

}
