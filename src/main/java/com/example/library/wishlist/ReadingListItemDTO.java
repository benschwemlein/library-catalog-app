package com.example.library.wishlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingListItemDTO {

    private Long itemId;
    private Long listId;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private String authorNames;
    private LocalDate addedDate;
    private ItemPriority priority;
    private String notes;
    private boolean read;
    private LocalDate readDate;
    private int sortOrder;
}
