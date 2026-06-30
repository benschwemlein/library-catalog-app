package com.example.library.acquisition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDTO {

    private Long id;
    private String supplier;
    private LocalDate orderDate;
    private LocalDate expectedDelivery;
    private LocalDate actualDelivery;
    private BigDecimal totalCost;
    private PurchaseOrderStatus status;
    private String notes;
    private String submittedByName;
    private int requestCount;
    private List<Long> requestIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
