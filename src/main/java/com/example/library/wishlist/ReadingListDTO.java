package com.example.library.wishlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingListDTO {

    private Long id;
    private Long memberId;
    private String memberName;
    private String name;
    private String description;
    private ListVisibility visibility;
    private LocalDate createdDate;
    private long itemCount;
    private long readCount;
    private List<ReadingListItemDTO> items;
}
