package com.example.library.bookclub;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookClubMeetingDTO {
    private Long id;
    private Long clubId;
    private Long discussedBookId;
    private String discussedBookTitle;
    private LocalDateTime meetingDate;
    private String location;
    private String notes;
    private int attendanceCount;
    private LocalDateTime createdAt;
}
