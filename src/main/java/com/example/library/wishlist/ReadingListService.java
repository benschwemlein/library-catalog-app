package com.example.library.wishlist;

import com.example.library.entity.Book;
import com.example.library.entity.Member;
import com.example.library.repository.BookRepository;
import com.example.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReadingListService {

    private final ReadingListRepository listRepo;
    private final ReadingListItemRepository itemRepo;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    public ReadingListDTO createList(Long memberId, String name, String description, ListVisibility visibility) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found: " + memberId));

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("List name must not be blank");
        }

        ListVisibility effectiveVisibility = (visibility != null) ? visibility : ListVisibility.PRIVATE;

        ReadingList list = ReadingList.builder()
                .member(member)
                .name(name.trim())
                .description(description)
                .visibility(effectiveVisibility)
                .createdDate(LocalDate.now())
                .build();

        ReadingList saved = listRepo.save(list);
        log.debug("Created reading list '{}' for member {}", saved.getName(), memberId);
        return toDTO(saved, false);
    }

    @Transactional(readOnly = true)
    public ReadingListDTO getList(Long listId, Long requestingMemberId) {
        ReadingList list = listRepo.findById(listId)
                .orElseThrow(() -> new RuntimeException("Reading list not found: " + listId));

        if (list.getVisibility() == ListVisibility.PRIVATE
                && !list.getMember().getId().equals(requestingMemberId)) {
            throw new IllegalStateException("Access denied to private list");
        }

        return toDTO(list, true);
    }

    @Transactional(readOnly = true)
    public List<ReadingListDTO> getMemberLists(Long memberId, Long requestingMemberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found: " + memberId));

        List<ReadingList> lists;
        if (memberId.equals(requestingMemberId)) {
            lists = listRepo.findByMemberId(memberId);
        } else {
            lists = listRepo.findByMemberIdAndVisibility(memberId, ListVisibility.PUBLIC);
        }

        return lists.stream()
                .map(l -> toDTO(l, false))
                .collect(Collectors.toList());
    }

    public ReadingListItemDTO addBook(Long listId, Long bookId, ItemPriority priority, String notes,
                                     Long requestingMemberId) {
        ReadingList list = listRepo.findById(listId)
                .orElseThrow(() -> new RuntimeException("Reading list not found: " + listId));

        if (!list.getMember().getId().equals(requestingMemberId)) {
            throw new IllegalStateException("Only the list owner can add books");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));

        itemRepo.findByReadingListIdAndBookId(listId, bookId).ifPresent(existing -> {
            throw new IllegalStateException("Book already in this reading list");
        });

        long currentCount = itemRepo.countByReadingListId(listId);
        ItemPriority effectivePriority = (priority != null) ? priority : ItemPriority.MEDIUM;

        ReadingListItem item = ReadingListItem.builder()
                .readingList(list)
                .book(book)
                .addedDate(LocalDate.now())
                .priority(effectivePriority)
                .notes(notes)
                .sortOrder((int) currentCount)
                .build();

        ReadingListItem saved = itemRepo.save(item);
        log.debug("Added book {} to reading list {}", bookId, listId);
        return toItemDTO(saved);
    }

    public void removeBook(Long listId, Long bookId, Long requestingMemberId) {
        ReadingList list = listRepo.findById(listId)
                .orElseThrow(() -> new RuntimeException("Reading list not found: " + listId));

        if (!list.getMember().getId().equals(requestingMemberId)) {
            throw new IllegalStateException("Only the list owner can remove books");
        }

        ReadingListItem item = itemRepo.findByReadingListIdAndBookId(listId, bookId)
                .orElseThrow(() -> new RuntimeException("Book not in this reading list"));

        itemRepo.delete(item);
        log.debug("Removed book {} from reading list {}", bookId, listId);
    }

    public ReadingListItemDTO markRead(Long listId, Long bookId, Long requestingMemberId) {
        ReadingList list = listRepo.findById(listId)
                .orElseThrow(() -> new RuntimeException("Reading list not found: " + listId));

        if (!list.getMember().getId().equals(requestingMemberId)) {
            throw new IllegalStateException("Only the list owner can mark books as read");
        }

        ReadingListItem item = itemRepo.findByReadingListIdAndBookId(listId, bookId)
                .orElseThrow(() -> new RuntimeException("Book not in this reading list"));

        item.setRead(true);
        item.setReadDate(LocalDate.now());

        ReadingListItem saved = itemRepo.save(item);
        log.debug("Marked book {} as read in reading list {}", bookId, listId);
        return toItemDTO(saved);
    }

    public ReadingListDTO updateList(Long listId, String name, String description,
                                    ListVisibility visibility, Long requestingMemberId) {
        ReadingList list = listRepo.findById(listId)
                .orElseThrow(() -> new RuntimeException("Reading list not found: " + listId));

        if (!list.getMember().getId().equals(requestingMemberId)) {
            throw new IllegalStateException("Only the list owner can update this list");
        }

        if (name != null && !name.isBlank()) {
            list.setName(name.trim());
        }
        if (description != null) {
            list.setDescription(description);
        }
        if (visibility != null) {
            list.setVisibility(visibility);
        }

        ReadingList saved = listRepo.save(list);
        log.debug("Updated reading list {}", listId);
        return toDTO(saved, false);
    }

    public void deleteList(Long listId, Long requestingMemberId) {
        ReadingList list = listRepo.findById(listId)
                .orElseThrow(() -> new RuntimeException("Reading list not found: " + listId));

        if (!list.getMember().getId().equals(requestingMemberId)) {
            throw new IllegalStateException("Only the list owner can delete this list");
        }

        listRepo.delete(list);
        log.debug("Deleted reading list {}", listId);
    }

    @Transactional(readOnly = true)
    public Page<ReadingListDTO> getPublicLists(Pageable pageable) {
        return listRepo.findByVisibility(ListVisibility.PUBLIC, pageable)
                .map(l -> toDTO(l, false));
    }

    public ReadingListDTO copyList(Long sourceListId, Long targetMemberId, String newName) {
        ReadingList source = listRepo.findById(sourceListId)
                .orElseThrow(() -> new RuntimeException("Reading list not found: " + sourceListId));

        Member targetMember = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new RuntimeException("Member not found: " + targetMemberId));

        String effectiveName = (newName != null && !newName.isBlank())
                ? newName.trim()
                : source.getName() + " (Copy)";

        ReadingList newList = ReadingList.builder()
                .member(targetMember)
                .name(effectiveName)
                .description(source.getDescription())
                .visibility(ListVisibility.PRIVATE)
                .createdDate(LocalDate.now())
                .build();

        ReadingList savedList = listRepo.save(newList);

        List<ReadingListItem> sourceItems = itemRepo.findByReadingListIdOrderBySortOrder(sourceListId);
        for (ReadingListItem sourceItem : sourceItems) {
            ReadingListItem newItem = ReadingListItem.builder()
                    .readingList(savedList)
                    .book(sourceItem.getBook())
                    .addedDate(LocalDate.now())
                    .priority(sourceItem.getPriority())
                    .notes(sourceItem.getNotes())
                    .sortOrder(sourceItem.getSortOrder())
                    .read(false)
                    .build();
            itemRepo.save(newItem);
        }

        log.debug("Copied reading list {} to member {} as '{}'", sourceListId, targetMemberId, effectiveName);
        return toDTO(savedList, false);
    }

    private ReadingListDTO toDTO(ReadingList list, boolean includeItems) {
        List<ReadingListItemDTO> items = null;
        if (includeItems) {
            items = itemRepo.findByReadingListIdOrderBySortOrder(list.getId())
                    .stream()
                    .map(this::toItemDTO)
                    .collect(Collectors.toList());
        }

        return ReadingListDTO.builder()
                .id(list.getId())
                .memberId(list.getMember().getId())
                .memberName(getMemberName(list.getMember()))
                .name(list.getName())
                .description(list.getDescription())
                .visibility(list.getVisibility())
                .createdDate(list.getCreatedDate())
                .itemCount(itemRepo.countByReadingListId(list.getId()))
                .readCount(itemRepo.countByReadingListIdAndReadTrue(list.getId()))
                .items(items)
                .build();
    }

    private ReadingListItemDTO toItemDTO(ReadingListItem item) {
        return ReadingListItemDTO.builder()
                .itemId(item.getId())
                .listId(item.getReadingList().getId())
                .bookId(item.getBook().getId())
                .bookTitle(item.getBook().getTitle())
                .bookIsbn(item.getBook().getIsbn())
                .authorNames(getAuthorNames(item.getBook()))
                .addedDate(item.getAddedDate())
                .priority(item.getPriority())
                .notes(item.getNotes())
                .read(item.isRead())
                .readDate(item.getReadDate())
                .sortOrder(item.getSortOrder())
                .build();
    }

    private String getMemberName(Member m) {
        if (m.getUser() != null) {
            String first = m.getUser().getFirstName() != null ? m.getUser().getFirstName() : "";
            String last = m.getUser().getLastName() != null ? m.getUser().getLastName() : "";
            String fullName = (first + " " + last).trim();
            return fullName.isEmpty() ? "Member #" + m.getId() : fullName;
        }
        return "Member #" + m.getId();
    }

    private String getAuthorNames(Book book) {
        if (book.getAuthors() == null || book.getAuthors().isEmpty()) {
            return "";
        }
        return book.getAuthors().stream()
                .map(a -> {
                    String first = a.getFirstName() != null ? a.getFirstName() : "";
                    String last = a.getLastName() != null ? a.getLastName() : "";
                    return (first + " " + last).trim();
                })
                .filter(name -> !name.isEmpty())
                .collect(Collectors.joining(", "));
    }
}
