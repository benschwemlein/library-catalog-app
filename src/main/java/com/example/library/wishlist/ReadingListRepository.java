package com.example.library.wishlist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadingListRepository extends JpaRepository<ReadingList, Long> {

    List<ReadingList> findByMemberId(Long memberId);

    Page<ReadingList> findByVisibility(ListVisibility visibility, Pageable pageable);

    List<ReadingList> findByMemberIdAndVisibility(Long memberId, ListVisibility visibility);
}
