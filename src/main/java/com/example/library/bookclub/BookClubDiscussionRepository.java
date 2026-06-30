package com.example.library.bookclub;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookClubDiscussionRepository extends JpaRepository<BookClubDiscussion, Long> {
    List<BookClubDiscussion> findByClubIdAndParentDiscussionIsNullOrderByPostedAtDesc(Long clubId);
    List<BookClubDiscussion> findByParentDiscussionIdOrderByPostedAtAsc(Long parentId);
    long countByClubId(Long clubId);
}
