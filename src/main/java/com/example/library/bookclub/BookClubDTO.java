package com.example.library.bookclub;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookClubDTO {
    private Long id;
    private String name;
    private String description;
    private Long branchId;
    private String branchName;
    private Long facilitatorId;
    private String facilitatorName;
    private int maxMembers;
    private int currentMemberCount;
    private String meetingSchedule;
    private Long currentBookId;
    private String currentBookTitle;
    private BookClubStatus status;
    private LocalDateTime createdAt;
}
