package com.example.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "search_log")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String query;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "result_count", nullable = false)
    private int resultCount;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "session_id", length = 100)
    private String sessionId;
}
