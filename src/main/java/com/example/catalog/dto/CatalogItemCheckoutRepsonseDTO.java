package com.example.catalog.dto;

import java.time.LocalDateTime;

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
public class CatalogItemCheckoutRepsonseDTO {
    private LocalDateTime checkoutDateTime;
    private UserDTO checkedoutBy;
}
