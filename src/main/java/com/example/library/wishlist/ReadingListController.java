package com.example.library.wishlist;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/library/reading-lists")
@Slf4j
@RequiredArgsConstructor
public class ReadingListController {

    private final ReadingListService readingListService;

    // -------------------------------------------------------------------------
    // Request body inner classes
    // -------------------------------------------------------------------------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateListRequest {
        @NotNull
        private Long memberId;
        @NotBlank
        private String name;
        private String description;
        private ListVisibility visibility;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateListRequest {
        private String name;
        private String description;
        private ListVisibility visibility;
        private Long requestingMemberId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddBookRequest {
        private Long bookId;
        private ItemPriority priority;
        private String notes;
        private Long requestingMemberId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CopyListRequest {
        private Long targetMemberId;
        private String newName;
    }

    // -------------------------------------------------------------------------
    // Endpoints
    // -------------------------------------------------------------------------

    /**
     * GET /api/library/reading-lists
     * Returns a page of all public reading lists.
     */
    @GetMapping
    public ResponseEntity<Page<ReadingListDTO>> getPublicLists(
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("GET public reading lists, page={}", pageable.getPageNumber());
        Page<ReadingListDTO> page = readingListService.getPublicLists(pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * GET /api/library/reading-lists/member/{memberId}
     * Returns reading lists for a specific member. Respects visibility rules.
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<ReadingListDTO>> getMemberLists(
            @PathVariable Long memberId,
            @RequestParam Long requestingMemberId) {
        log.debug("GET lists for member {}, requested by {}", memberId, requestingMemberId);
        List<ReadingListDTO> lists = readingListService.getMemberLists(memberId, requestingMemberId);
        return ResponseEntity.ok(lists);
    }

    /**
     * POST /api/library/reading-lists
     * Creates a new reading list for the given member.
     */
    @PostMapping
    public ResponseEntity<ReadingListDTO> createList(@Valid @RequestBody CreateListRequest request) {
        log.debug("POST create reading list '{}' for member {}", request.getName(), request.getMemberId());
        ReadingListDTO created = readingListService.createList(
                request.getMemberId(),
                request.getName(),
                request.getDescription(),
                request.getVisibility()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/library/reading-lists/{listId}
     * Returns a single reading list with all its items (detail view).
     */
    @GetMapping("/{listId}")
    public ResponseEntity<ReadingListDTO> getList(
            @PathVariable Long listId,
            @RequestParam Long requestingMemberId) {
        log.debug("GET reading list {}, requested by {}", listId, requestingMemberId);
        ReadingListDTO dto = readingListService.getList(listId, requestingMemberId);
        return ResponseEntity.ok(dto);
    }

    /**
     * PUT /api/library/reading-lists/{listId}
     * Updates a reading list's name, description, or visibility.
     */
    @PutMapping("/{listId}")
    public ResponseEntity<ReadingListDTO> updateList(
            @PathVariable Long listId,
            @RequestBody UpdateListRequest request) {
        log.debug("PUT update reading list {}", listId);
        ReadingListDTO updated = readingListService.updateList(
                listId,
                request.getName(),
                request.getDescription(),
                request.getVisibility(),
                request.getRequestingMemberId()
        );
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/library/reading-lists/{listId}
     * Deletes a reading list and all its items.
     */
    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteList(
            @PathVariable Long listId,
            @RequestParam Long requestingMemberId) {
        log.debug("DELETE reading list {}", listId);
        readingListService.deleteList(listId, requestingMemberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/library/reading-lists/{listId}/books
     * Adds a book to a reading list.
     */
    @PostMapping("/{listId}/books")
    public ResponseEntity<ReadingListItemDTO> addBook(
            @PathVariable Long listId,
            @RequestBody AddBookRequest request) {
        log.debug("POST add book {} to reading list {}", request.getBookId(), listId);
        ReadingListItemDTO item = readingListService.addBook(
                listId,
                request.getBookId(),
                request.getPriority(),
                request.getNotes(),
                request.getRequestingMemberId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    /**
     * DELETE /api/library/reading-lists/{listId}/books/{bookId}
     * Removes a book from a reading list.
     */
    @DeleteMapping("/{listId}/books/{bookId}")
    public ResponseEntity<Void> removeBook(
            @PathVariable Long listId,
            @PathVariable Long bookId,
            @RequestParam Long requestingMemberId) {
        log.debug("DELETE book {} from reading list {}", bookId, listId);
        readingListService.removeBook(listId, bookId, requestingMemberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/library/reading-lists/{listId}/books/{bookId}/read
     * Marks a book in a reading list as read, recording today's date.
     */
    @PutMapping("/{listId}/books/{bookId}/read")
    public ResponseEntity<ReadingListItemDTO> markRead(
            @PathVariable Long listId,
            @PathVariable Long bookId,
            @RequestParam Long requestingMemberId) {
        log.debug("PUT mark book {} as read in reading list {}", bookId, listId);
        ReadingListItemDTO item = readingListService.markRead(listId, bookId, requestingMemberId);
        return ResponseEntity.ok(item);
    }

    /**
     * POST /api/library/reading-lists/{listId}/copy
     * Copies a reading list to a target member as a new private list.
     */
    @PostMapping("/{listId}/copy")
    public ResponseEntity<ReadingListDTO> copyList(
            @PathVariable Long listId,
            @RequestBody CopyListRequest request) {
        log.debug("POST copy reading list {} to member {}", listId, request.getTargetMemberId());
        ReadingListDTO copied = readingListService.copyList(
                listId,
                request.getTargetMemberId(),
                request.getNewName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(copied);
    }
}
