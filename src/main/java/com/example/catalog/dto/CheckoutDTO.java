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
public class CheckoutDTO {

    private Long id;

    private CatalogItemRequestDTO item;

    private Boolean checkedOut;
    private LocalDateTime checkoutDateTime;
    private LocalDateTime checkinDateTime;
    private UserDTO checkedoutBy;

}
