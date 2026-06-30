package com.example.library.bookclub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/library/book-clubs")
@Slf4j
@RequiredArgsConstructor
public class BookClubController {

    private final BookClubService clubService;

    @GetMapping
    public ResponseEntity<List<BookClubDTO>> getAllClubs() {
        return ResponseEntity.ok(clubService.searchClubs(""));
    }

    @PostMapping("/")
    public ResponseEntity<?> createClub(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            String description = (String) body.get("description");
            Long branchId = toLong(body.get("branchId"));
            Long facilitatorId = toLong(body.get("facilitatorId"));
            int maxMembers = body.containsKey("maxMembers") ? toInt(body.get("maxMembers")) : 20;
            String meetingSchedule = (String) body.get("meetingSchedule");

            log.info("Creating book club '{}' for branchId={}", name, branchId);
            BookClubDTO dto = clubService.createClub(name, description, branchId, facilitatorId,
                    maxMembers, meetingSchedule);
            return ResponseEntity.status(201).body(dto);
        } catch (RuntimeException e) {
            log.warn("Failed to create book club: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{clubId}")
    public ResponseEntity<?> getClub(@PathVariable Long clubId) {
        try {
            log.info("Getting book club id={}", clubId);
            return ResponseEntity.ok(clubService.getClub(clubId));
        } catch (RuntimeException e) {
            log.warn("Book club not found id={}: {}", clubId, e.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<?> getClubsByBranch(@PathVariable Long branchId) {
        try {
            log.info("Getting book clubs for branchId={}", branchId);
            return ResponseEntity.ok(clubService.getClubsByBranch(branchId));
        } catch (RuntimeException e) {
            log.warn("Failed to get clubs for branchId={}: {}", branchId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchClubs(@RequestParam String query) {
        try {
            log.info("Searching book clubs with query='{}'", query);
            return ResponseEntity.ok(clubService.searchClubs(query));
        } catch (RuntimeException e) {
            log.warn("Failed to search clubs: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{clubId}/join")
    public ResponseEntity<?> joinClub(@PathVariable Long clubId, @RequestBody Map<String, Long> body) {
        try {
            Long memberId = body.get("memberId");
            log.info("Member id={} joining club id={}", memberId, clubId);
            BookClubMembership membership = clubService.joinClub(clubId, memberId);
            return ResponseEntity.ok(Map.of("membershipId", membership.getId(), "clubId", clubId, "memberId", memberId));
        } catch (RuntimeException e) {
            log.warn("Failed to join club id={}: {}", clubId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{clubId}/leave")
    public ResponseEntity<?> leaveClub(@PathVariable Long clubId, @RequestBody Map<String, Long> body) {
        try {
            Long memberId = body.get("memberId");
            log.info("Member id={} leaving club id={}", memberId, clubId);
            clubService.leaveClub(clubId, memberId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Failed to leave club id={}: {}", clubId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{clubId}/meetings")
    public ResponseEntity<?> scheduleMeeting(@PathVariable Long clubId, @RequestBody Map<String, Object> body) {
        try {
            String meetingDateStr = (String) body.get("meetingDate");
            LocalDateTime meetingDate = LocalDateTime.parse(meetingDateStr);
            String location = (String) body.get("location");
            Long bookId = toLong(body.get("bookId"));

            log.info("Scheduling meeting for club id={} on {}", clubId, meetingDate);
            BookClubMeeting meeting = clubService.scheduleMeeting(clubId, meetingDate, location, bookId);
            return ResponseEntity.status(201).body(clubService.toMeetingDTO(meeting));
        } catch (RuntimeException e) {
            log.warn("Failed to schedule meeting for club id={}: {}", clubId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{clubId}/meetings")
    public ResponseEntity<?> getUpcomingMeetings(@PathVariable Long clubId) {
        try {
            log.info("Getting upcoming meetings for club id={}", clubId);
            List<BookClubMeetingDTO> dtos = clubService.getUpcomingMeetings(clubId).stream()
                    .map(clubService::toMeetingDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            log.warn("Failed to get meetings for club id={}: {}", clubId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{clubId}/discussions")
    public ResponseEntity<?> postDiscussion(@PathVariable Long clubId, @RequestBody Map<String, Object> body) {
        try {
            Long memberId = toLong(body.get("memberId"));
            String content = (String) body.get("content");
            Long parentDiscussionId = toLong(body.get("parentDiscussionId"));

            log.info("Member id={} posting discussion in club id={}", memberId, clubId);
            DiscussionPostDTO dto = clubService.postDiscussion(clubId, memberId, content, parentDiscussionId);
            return ResponseEntity.status(201).body(dto);
        } catch (RuntimeException e) {
            log.warn("Failed to post discussion in club id={}: {}", clubId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{clubId}/discussions")
    public ResponseEntity<?> getDiscussions(@PathVariable Long clubId,
                                             @RequestParam(required = false) Long meetingId) {
        try {
            log.info("Getting discussions for club id={}, meetingId={}", clubId, meetingId);
            return ResponseEntity.ok(clubService.getDiscussions(clubId, meetingId));
        } catch (RuntimeException e) {
            log.warn("Failed to get discussions for club id={}: {}", clubId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/discussions/{discussionId}/replies")
    public ResponseEntity<?> getReplies(@PathVariable Long discussionId) {
        try {
            log.info("Getting replies for discussion id={}", discussionId);
            return ResponseEntity.ok(clubService.getReplies(discussionId));
        } catch (RuntimeException e) {
            log.warn("Failed to get replies for discussion id={}: {}", discussionId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{clubId}/current-book")
    public ResponseEntity<?> setCurrentBook(@PathVariable Long clubId, @RequestBody Map<String, Long> body) {
        try {
            Long bookId = body.get("bookId");
            log.info("Setting current book id={} for club id={}", bookId, clubId);
            return ResponseEntity.ok(clubService.setCurrentBook(clubId, bookId));
        } catch (RuntimeException e) {
            log.warn("Failed to set current book for club id={}: {}", clubId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- Utility helpers ---

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }

    private int toInt(Object value) {
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }
}
