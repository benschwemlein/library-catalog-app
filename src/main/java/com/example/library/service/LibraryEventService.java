package com.example.library.service;

import com.example.library.dto.CreateEventRequest;
import com.example.library.dto.LibraryEventDTO;
import com.example.library.entity.*;
import com.example.library.exception.MemberNotFoundException;
import com.example.library.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LibraryEventService {

    @Autowired
    private LibraryEventRepository libraryEventRepository;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private LibraryBranchRepository libraryBranchRepository;

    @Autowired
    private MemberRepository memberRepository;

    private LibraryEventDTO toDTO(LibraryEvent event) {
        LibraryEventDTO dto = new LibraryEventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartDateTime(event.getStartDateTime());
        dto.setEndDateTime(event.getEndDateTime());
        dto.setCapacity(event.getCapacity());
        dto.setRegisteredCount(event.getRegisteredCount());
        dto.setEventType(event.getEventType() != null ? event.getEventType().name() : null);
        dto.setSpotsAvailable(event.getCapacity() - event.getRegisteredCount());
        if (event.getBranch() != null) {
            dto.setBranchId(event.getBranch().getId());
            dto.setBranchName(event.getBranch().getName());
        }
        return dto;
    }

    public List<LibraryEventDTO> getAllEvents() {
        return libraryEventRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public LibraryEventDTO findById(Long id) {
        LibraryEvent event = libraryEventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + id));
        return toDTO(event);
    }

    public LibraryEventDTO create(LibraryEventDTO dto) {
        LibraryEvent event = new LibraryEvent();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartDateTime(dto.getStartDateTime());
        event.setEndDateTime(dto.getEndDateTime());
        event.setCapacity(dto.getCapacity());
        if (dto.getEventType() != null) {
            event.setEventType(EventType.valueOf(dto.getEventType()));
        }
        if (dto.getBranchId() != null) {
            libraryBranchRepository.findById(dto.getBranchId())
                    .ifPresent(event::setBranch);
        }
        return toDTO(libraryEventRepository.save(event));
    }

    public LibraryEventDTO update(Long id, LibraryEventDTO dto) {
        LibraryEvent event = libraryEventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + id));
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartDateTime(dto.getStartDateTime());
        event.setEndDateTime(dto.getEndDateTime());
        event.setCapacity(dto.getCapacity());
        if (dto.getEventType() != null) {
            event.setEventType(EventType.valueOf(dto.getEventType()));
        }
        if (dto.getBranchId() != null) {
            libraryBranchRepository.findById(dto.getBranchId())
                    .ifPresent(event::setBranch);
        }
        return toDTO(libraryEventRepository.save(event));
    }

    public void delete(Long id) {
        LibraryEvent event = libraryEventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + id));
        libraryEventRepository.delete(event);
    }

    public LibraryEventDTO register(Long eventId, Long memberId) {
        LibraryEvent event = libraryEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found: " + memberId));

        boolean alreadyRegistered = eventRegistrationRepository.existsByEvent_IdAndMember_Id(eventId, memberId);
        if (alreadyRegistered) {
            throw new IllegalStateException("Member is already registered for this event.");
        }

        long currentRegistrations = eventRegistrationRepository.countByEvent_Id(eventId);
        if (currentRegistrations >= event.getCapacity()) {
            throw new IllegalStateException("Event is at full capacity.");
        }

        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);
        registration.setMember(member);
        registration.setRegistrationDate(LocalDateTime.now());

        eventRegistrationRepository.save(registration);
        event.setRegisteredCount(event.getRegisteredCount() + 1);
        return toDTO(libraryEventRepository.save(event));
    }

    public void unregister(Long eventId, Long memberId) {
        EventRegistration registration = eventRegistrationRepository
                .findByEvent_IdAndMember_Id(eventId, memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Registration not found for event " + eventId + " and member " + memberId));
        eventRegistrationRepository.delete(registration);
        libraryEventRepository.findById(eventId).ifPresent(event -> {
            event.setRegisteredCount(Math.max(0, event.getRegisteredCount() - 1));
            libraryEventRepository.save(event);
        });
    }

    public LibraryEvent createEvent(CreateEventRequest req) {
        LibraryEvent event = new LibraryEvent();
        event.setTitle(req.getTitle());
        event.setDescription(req.getDescription());
        event.setStartDateTime(req.getStartDateTime());
        event.setEndDateTime(req.getEndDateTime());
        event.setCapacity(req.getCapacity());
        if (req.getEventType() != null) {
            event.setEventType(EventType.valueOf(req.getEventType()));
        }
        if (req.getBranchId() != null) {
            libraryBranchRepository.findById(req.getBranchId())
                    .ifPresent(event::setBranch);
        }
        return libraryEventRepository.save(event);
    }

    public LibraryEvent updateEvent(Long id, CreateEventRequest req) {
        LibraryEvent event = libraryEventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + id));
        event.setTitle(req.getTitle());
        event.setDescription(req.getDescription());
        event.setStartDateTime(req.getStartDateTime());
        event.setEndDateTime(req.getEndDateTime());
        event.setCapacity(req.getCapacity());
        if (req.getEventType() != null) {
            event.setEventType(EventType.valueOf(req.getEventType()));
        }
        if (req.getBranchId() != null) {
            libraryBranchRepository.findById(req.getBranchId())
                    .ifPresent(event::setBranch);
        }
        return libraryEventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        delete(id);
    }

    @Transactional(readOnly = true)
    public List<LibraryEvent> getUpcomingEvents() {
        return libraryEventRepository.findUpcomingEvents(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<LibraryEvent> getEventsByBranch(Long branchId) {
        return libraryEventRepository.findByBranch_IdAndStartDateTimeAfter(branchId, LocalDateTime.now().minusYears(10));
    }
}
