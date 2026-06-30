package com.example.library.pattern.command;

import com.example.library.entity.BookCopy;
import com.example.library.entity.LibraryBranch;
import com.example.library.repository.BookCopyRepository;
import com.example.library.repository.LibraryBranchRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Command that transfers a book copy from one branch to another.
 *
 * <p>Prototype-scoped; set {@code copyId}, {@code fromBranchId}, and {@code toBranchId}
 * before calling {@code execute()}.</p>
 */
@Component
@Scope("prototype")
@Slf4j
public class TransferCopyCommand implements LibraryCommand {

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private LibraryBranchRepository libraryBranchRepository;

    @Setter private Long copyId;
    @Setter private Long fromBranchId;
    @Setter private Long toBranchId;

    @Override
    public CommandResult execute() {
        if (copyId == null || fromBranchId == null || toBranchId == null) {
            return CommandResult.failure("Cannot execute: copyId, fromBranchId, and toBranchId are required");
        }
        try {
            BookCopy copy = bookCopyRepository.findById(copyId)
                    .orElseThrow(() -> new IllegalArgumentException("BookCopy not found: " + copyId));

            LibraryBranch toBranch = libraryBranchRepository.findById(toBranchId)
                    .orElseThrow(() -> new IllegalArgumentException("Destination branch not found: " + toBranchId));

            // Validate the from-branch matches the copy's current branch
            if (copy.getBranch() != null && !copy.getBranch().getId().equals(fromBranchId)) {
                return CommandResult.failure(
                        String.format("Copy %d is at branch %d, not at expected fromBranch %d",
                                copyId, copy.getBranch().getId(), fromBranchId),
                        copyId
                );
            }

            copy.setBranch(toBranch);
            bookCopyRepository.save(copy);

            log.info("TransferCopyCommand executed: copyId={} fromBranchId={} toBranchId={}",
                    copyId, fromBranchId, toBranchId);

            return CommandResult.success(
                    String.format("Copy %d (barcode=%s) transferred to branch '%s'",
                            copyId, copy.getBarcode(), toBranch.getName()),
                    copyId
            );
        } catch (Exception e) {
            log.error("TransferCopyCommand failed: copyId={} fromBranchId={} toBranchId={} error={}",
                    copyId, fromBranchId, toBranchId, e.getMessage(), e);
            return CommandResult.failure("Transfer failed: " + e.getMessage(), copyId);
        }
    }

    @Override
    public CommandResult undo() {
        if (copyId == null || fromBranchId == null) {
            return CommandResult.failure("Cannot undo: insufficient state (was execute() called?)");
        }
        try {
            BookCopy copy = bookCopyRepository.findById(copyId)
                    .orElseThrow(() -> new IllegalArgumentException("BookCopy not found: " + copyId));

            LibraryBranch fromBranch = libraryBranchRepository.findById(fromBranchId)
                    .orElseThrow(() -> new IllegalArgumentException("Origin branch not found: " + fromBranchId));

            copy.setBranch(fromBranch);
            bookCopyRepository.save(copy);

            log.info("TransferCopyCommand undone: copyId={} restoredToBranchId={}", copyId, fromBranchId);

            return CommandResult.success(
                    String.format("Transfer reversed. Copy %d returned to branch '%s'",
                            copyId, fromBranch.getName()),
                    copyId
            );
        } catch (Exception e) {
            log.error("TransferCopyCommand undo failed: copyId={} error={}", copyId, e.getMessage(), e);
            return CommandResult.failure("Undo transfer failed: " + e.getMessage(), copyId);
        }
    }

    @Override
    public String getDescription() {
        return String.format("TransferCopy copyId=%d from branchId=%d to branchId=%d",
                copyId, fromBranchId, toBranchId);
    }
}
