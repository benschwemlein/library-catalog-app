package com.example.library.interlibrary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitILLRequest {

    private Long memberId;
    private Long branchId;
    private String bookTitle;
    private String authorName;
    private String isbn;
    private LocalDate neededByDate;
    private String notes;
}
