package com.example.library.digitalresource;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "digital_resource")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    private DigitalResourceType resourceType;

    @Enumerated(EnumType.STRING)
    private DigitalResourceFormat format;

    @Column(length = 2000)
    private String fileUrl;

    private Long fileSizeBytes;

    private Integer durationMinutes;

    private String publisher;

    @Column(length = 20)
    private String isbn;

    @Enumerated(EnumType.STRING)
    private LicenseType licenseType;

    private Integer maxConcurrentUsers;

    private Integer publicationYear;

    @Column(length = 50)
    private String language;

    @Column(length = 2000)
    private String coverImageUrl;

    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DigitalLoan> loans = new ArrayList<>();
}
