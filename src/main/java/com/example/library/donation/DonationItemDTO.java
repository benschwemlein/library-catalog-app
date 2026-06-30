package com.example.library.donation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationItemDTO {

    private Long id;
    private Long donationId;
    private String title;
    private String author;
    private String isbn;
    private int quantity;
    private String condition;
    private ItemDisposition disposition;
    private String dispositionNotes;
    private Long addedBookId;
}
