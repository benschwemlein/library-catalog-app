package com.example.library.digitalresource;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalResourceDTO {

    private Long id;
    private String title;
    private String description;
    private DigitalResourceType resourceType;
    private DigitalResourceFormat format;
    private String fileUrl;
    private Long fileSizeBytes;
    private Integer durationMinutes;
    private String publisher;
    private String isbn;
    private LicenseType licenseType;
    private Integer maxConcurrentUsers;
    private Integer publicationYear;
    private String language;
    private String coverImageUrl;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean availableNow;
    private int activeLoans;
}
