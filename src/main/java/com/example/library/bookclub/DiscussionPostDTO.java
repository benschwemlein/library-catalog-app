package com.example.library.bookclub;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionPostDTO {
    private Long id;
    private Long clubId;
    private Long meetingId;
    private Long posterId;
    private String posterName;
    private String content;
    private LocalDateTime postedAt;
    private Long parentDiscussionId;
    private boolean edited;
    private LocalDateTime editedAt;
    private int replyCount;
}
