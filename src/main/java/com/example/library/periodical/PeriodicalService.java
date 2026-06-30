package com.example.library.periodical;

import com.example.library.entity.LibraryBranch;
import com.example.library.repository.LibraryBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PeriodicalService {

    private final PeriodicalRepository periodicalRepository;
    private final PeriodicalIssueRepository issueRepository;
    private final PeriodicalSubscriptionRepository subscriptionRepository;
    private final LibraryBranchRepository branchRepository;

    @Transactional(readOnly = true)
    public Page<PeriodicalDTO> searchPeriodicals(String query, String category, Pageable pageable) {
        Page<Periodical> page;

        if (query != null && !query.isBlank()) {
            page = periodicalRepository.findByTitleContainingIgnoreCase(query, pageable);
        } else {
            page = periodicalRepository.findByActiveTrue(pageable);
        }

        if (category != null && !category.isBlank()) {
            log.debug("Applying post-page category filter: {}", category);
            List<PeriodicalDTO> filtered = page.getContent().stream()
                    .filter(p -> category.equalsIgnoreCase(p.getCategory()))
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return new PageImpl<>(filtered, pageable, filtered.size());
        }

        return page.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public PeriodicalDTO getPeriodical(Long id) {
        Periodical periodical = periodicalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Periodical not found: " + id));
        return toDTO(periodical);
    }

    @Transactional
    public PeriodicalDTO createPeriodical(String title, String issn, String publisher,
                                          PeriodicalFrequency frequency, String category,
                                          String description, Long branchId,
                                          boolean digitalAccess, String digitalUrl) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank");
        }

        LibraryBranch branch = null;
        if (branchId != null) {
            branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
        }

        Periodical periodical = Periodical.builder()
                .title(title)
                .issn(issn)
                .publisher(publisher)
                .frequency(frequency)
                .category(category)
                .description(description)
                .branch(branch)
                .digitalAccess(digitalAccess)
                .digitalUrl(digitalUrl)
                .active(true)
                .build();

        Periodical saved = periodicalRepository.save(periodical);
        log.info("Created periodical id={} title={}", saved.getId(), saved.getTitle());
        return toDTO(saved);
    }

    @Transactional
    public PeriodicalIssueDTO addIssue(Long periodicalId, int volume, int issueNumber,
                                       LocalDate publicationDate, String condition, String location) {
        Periodical periodical = periodicalRepository.findById(periodicalId)
                .orElseThrow(() -> new RuntimeException("Periodical not found: " + periodicalId));

        issueRepository.findByPeriodicalIdAndVolumeAndIssueNumber(periodicalId, volume, issueNumber)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Issue already exists for volume " + volume + " number " + issueNumber);
                });

        PeriodicalIssue issue = PeriodicalIssue.builder()
                .periodical(periodical)
                .volume(volume)
                .issueNumber(issueNumber)
                .publicationDate(publicationDate)
                .status(PeriodicalIssueStatus.CURRENT)
                .condition(condition)
                .location(location)
                .receivedDate(LocalDate.now())
                .build();

        PeriodicalIssue saved = issueRepository.save(issue);
        log.info("Added issue id={} to periodical id={}", saved.getId(), periodicalId);
        return toIssueDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<PeriodicalIssueDTO> getIssues(Long periodicalId) {
        periodicalRepository.findById(periodicalId)
                .orElseThrow(() -> new RuntimeException("Periodical not found: " + periodicalId));
        return issueRepository.findByPeriodicalId(periodicalId).stream()
                .map(this::toIssueDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PeriodicalIssueDTO> getCurrentIssues(Long periodicalId) {
        periodicalRepository.findById(periodicalId)
                .orElseThrow(() -> new RuntimeException("Periodical not found: " + periodicalId));
        return issueRepository.findByPeriodicalIdAndStatus(periodicalId, PeriodicalIssueStatus.CURRENT).stream()
                .map(this::toIssueDTO)
                .toList();
    }

    @Transactional
    public PeriodicalIssueDTO updateIssueStatus(Long issueId, PeriodicalIssueStatus status) {
        PeriodicalIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found: " + issueId));
        issue.setStatus(status);
        PeriodicalIssue saved = issueRepository.save(issue);
        log.info("Updated issue id={} status to {}", issueId, status);
        return toIssueDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return periodicalRepository.findDistinctCategories();
    }

    @Transactional(readOnly = true)
    public List<PeriodicalDTO> getByBranch(Long branchId) {
        return periodicalRepository.findByBranchId(branchId).stream()
                .map(this::toDTO)
                .toList();
    }

    private PeriodicalDTO toDTO(Periodical p) {
        Long branchId = p.getBranch() != null ? p.getBranch().getId() : null;
        String branchName = p.getBranch() != null ? p.getBranch().getName() : null;
        int issueCount = p.getIssues() != null ? p.getIssues().size() : 0;

        return PeriodicalDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .issn(p.getIssn())
                .publisher(p.getPublisher())
                .frequency(p.getFrequency())
                .category(p.getCategory())
                .description(p.getDescription())
                .active(p.isActive())
                .digitalAccess(p.isDigitalAccess())
                .digitalUrl(p.getDigitalUrl())
                .branchId(branchId)
                .branchName(branchName)
                .issueCount(issueCount)
                .build();
    }

    private PeriodicalIssueDTO toIssueDTO(PeriodicalIssue i) {
        Long periodicalId = i.getPeriodical() != null ? i.getPeriodical().getId() : null;
        String periodicalTitle = i.getPeriodical() != null ? i.getPeriodical().getTitle() : null;

        return PeriodicalIssueDTO.builder()
                .id(i.getId())
                .periodicalId(periodicalId)
                .periodicalTitle(periodicalTitle)
                .volume(i.getVolume())
                .issueNumber(i.getIssueNumber())
                .publicationDate(i.getPublicationDate())
                .status(i.getStatus())
                .condition(i.getCondition())
                .location(i.getLocation())
                .receivedDate(i.getReceivedDate())
                .notes(i.getNotes())
                .build();
    }
}
