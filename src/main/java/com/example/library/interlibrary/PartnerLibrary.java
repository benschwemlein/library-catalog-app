package com.example.library.interlibrary;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "partner_library")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerLibrary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;
    private String city;
    private String state;
    private String contactEmail;
    private String contactPhone;

    @Builder.Default
    private int loanPeriodDays = 21;

    @Builder.Default
    private int shippingDays = 5;

    @Builder.Default
    private boolean active = true;

    @Column
    private String notes;

    private LocalDate memberSince;
}
