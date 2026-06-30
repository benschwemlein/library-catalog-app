package com.example.library.bookclub;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookClubMeetingRepository extends JpaRepository<BookClubMeeting, Long> {
    List<BookClubMeeting> findByClubIdOrderByMeetingDateDesc(Long clubId);
    List<BookClubMeeting> findByClubIdAndMeetingDateAfterOrderByMeetingDateAsc(Long clubId, LocalDateTime after);
}
