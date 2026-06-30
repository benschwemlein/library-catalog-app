package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotBlank(message = "Notification type is required")
    private String type;

    @NotBlank(message = "Message is required")
    private String message;

    private LocalDateTime sentDate;

    private LocalDateTime readDate;

    private String channel;

    private boolean isRead;
}
