package com.example.library.controller;

import com.example.library.dto.LibraryEventDTO;
import com.example.library.entity.EventRegistration;
import com.example.library.exception.CheckoutValidationException;
import jakarta.validation.Valid;
import com.example.library.entity.LibraryEvent;
import com.example.library.repository.EventRegistrationRepository;
import com.example.library.service.LibraryEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
public class LibraryEventController {

    @Autowired
    private LibraryEventService libraryEventService;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @GetMapping
    public ResponseEntity<List<LibraryEventDTO>> getAllEvents() {
        return ResponseEntity.ok(libraryEventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibraryEventDTO> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(libraryEventService.findById(id));
    }

    @PostMapping
    public ResponseEntity<LibraryEventDTO> createEvent(@Valid @RequestBody LibraryEventDTO eventDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEventService.create(eventDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LibraryEventDTO> updateEvent(@PathVariable Long id, @Valid @RequestBody LibraryEventDTO eventDTO) {
        return ResponseEntity.ok(libraryEventService.update(id, eventDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        libraryEventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<LibraryEventDTO> registerForEvent(@PathVariable Long id,
                                                             @RequestBody(required = false) Map<String, Object> body,
                                                             @RequestParam(required = false) Long memberId) {
        Long resolvedMemberId = memberId != null ? memberId
                : body != null && body.get("memberId") != null ? Long.valueOf(body.get("memberId").toString()) : null;
        if (resolvedMemberId == null) throw new CheckoutValidationException("memberId is required");
        return ResponseEntity.ok(libraryEventService.register(id, resolvedMemberId));
    }

    @DeleteMapping("/{id}/register")
    public ResponseEntity<Void> unregisterFromEvent(@PathVariable Long id, @RequestParam(required = false) Long memberId) {
        if (memberId != null) libraryEventService.unregister(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/register/{memberId}")
    public ResponseEntity<Void> unregisterByPath(@PathVariable Long id, @PathVariable Long memberId) {
        libraryEventService.unregister(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/member/{memberId}/registrations")
    public ResponseEntity<List<Map<String, Object>>> getMemberRegistrations(@PathVariable Long memberId) {
        List<EventRegistration> regs = eventRegistrationRepository.findByMember_Id(memberId);
        List<Map<String, Object>> result = regs.stream().map(r -> Map.<String, Object>of(
                "id", r.getId(),
                "eventId", r.getEvent().getId(),
                "eventTitle", r.getEvent().getTitle(),
                "registrationDate", r.getRegistrationDate().toString(),
                "attended", r.isAttended()
        )).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<LibraryEvent>> getUpcomingEvents(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        return ResponseEntity.ok(libraryEventService.getUpcomingEvents().stream()
                .limit(limit).toList());
    }

    @GetMapping("/{id}/registrations")
    public ResponseEntity<List<Map<String, Object>>> getEventRegistrations(@PathVariable Long id) {
        LibraryEventDTO event = libraryEventService.findById(id);
        return ResponseEntity.ok(List.of(Map.of(
                "eventId", id,
                "registeredCount", event.getRegisteredCount(),
                "capacity", event.getCapacity()
        )));
    }
}
