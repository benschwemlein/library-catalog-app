package com.example.library.donation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorInfo {

    @Column(name = "donor_name", nullable = false, length = 200)
    private String donorName;

    @Column(name = "donor_email", length = 200)
    private String donorEmail;

    @Column(name = "donor_phone", length = 50)
    private String donorPhone;

    @Column(name = "donor_address", length = 500)
    private String donorAddress;
}
