package com.example.library.digitalresource;

import com.example.library.entity.LibraryBranch;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "digital_license")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private DigitalResource resource;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private LibraryBranch branch;

    private LocalDate expiryDate;

    private int maxUsers;

    @Builder.Default
    private int currentUsers = 0;

    @Builder.Default
    private boolean active = true;
}
