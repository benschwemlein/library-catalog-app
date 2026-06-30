package com.example.library.service;

import com.example.library.dto.BookCopyConditionUpdateDTO;
import com.example.library.dto.BookCopyTransferDTO;
import com.example.library.dto.CreateCopyRequest;
import com.example.library.dto.TransferCopyRequest;
import com.example.library.entity.Book;
import com.example.library.entity.BookCopy;
import com.example.library.entity.CopyCondition;
import com.example.library.entity.CopyStatus;
import com.example.library.entity.LibraryBranch;
import com.example.library.exception.BookNotFoundException;
import com.example.library.repository.BookCopyRepository;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LibraryBranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookCopyService {

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LibraryBranchRepository libraryBranchRepository;

    public BookCopy create(Object dto) {
        // generic stub for controller
        return new BookCopy();
    }

    public BookCopy addCopy(CreateCopyRequest req) {
        Book book = bookRepository.findById(req.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + req.getBookId()));

        BookCopy copy = new BookCopy();
        copy.setBook(book);
        copy.setStatus(CopyStatus.AVAILABLE);
        if (req.getCondition() != null) {
            try {
                copy.setCondition(CopyCondition.valueOf(req.getCondition().toUpperCase()));
            } catch (IllegalArgumentException e) {
                copy.setCondition(CopyCondition.GOOD);
            }
        } else {
            copy.setCondition(CopyCondition.GOOD);
        }
        copy.setBarcode(req.getBarcode());

        if (req.getBranchId() != null) {
            libraryBranchRepository.findById(req.getBranchId())
                    .ifPresent(copy::setBranch);
        }

        return bookCopyRepository.save(copy);
    }

    public BookCopy withdrawCopy(Long copyId) {
        BookCopy copy = findById(copyId);
        if (CopyStatus.CHECKED_OUT.equals(copy.getStatus())) {
            throw new IllegalStateException("Cannot withdraw a copy that is currently checked out.");
        }
        copy.setStatus(CopyStatus.WITHDRAWN);
        return bookCopyRepository.save(copy);
    }

    public BookCopy delete(Long copyId) {
        BookCopy copy = findById(copyId);
        bookCopyRepository.delete(copy);
        return copy;
    }

    public BookCopy updateCondition(Long copyId, BookCopyConditionUpdateDTO update) {
        BookCopy copy = findById(copyId);
        if (update.getCondition() != null) {
            try {
                copy.setCondition(CopyCondition.valueOf(update.getCondition().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // ignore invalid condition
            }
        }
        return bookCopyRepository.save(copy);
    }

    public BookCopy updateCondition(Long copyId, String condition) {
        BookCopy copy = findById(copyId);
        try {
            copy.setCondition(CopyCondition.valueOf(condition.toUpperCase()));
        } catch (IllegalArgumentException e) {
            // ignore invalid condition
        }
        return bookCopyRepository.save(copy);
    }

    public BookCopy transfer(Long copyId, BookCopyTransferDTO transfer) {
        BookCopy copy = findById(copyId);
        if (CopyStatus.CHECKED_OUT.equals(copy.getStatus())) {
            throw new IllegalStateException("Cannot transfer a copy that is currently checked out.");
        }
        if (transfer.getTargetBranchId() != null) {
            LibraryBranch targetBranch = libraryBranchRepository.findById(transfer.getTargetBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Target branch not found: " + transfer.getTargetBranchId()));
            copy.setBranch(targetBranch);
        }
        return bookCopyRepository.save(copy);
    }

    public BookCopy transferCopy(Long copyId, TransferCopyRequest req) {
        BookCopy copy = findById(copyId);
        if (CopyStatus.CHECKED_OUT.equals(copy.getStatus())) {
            throw new IllegalStateException("Cannot transfer a copy that is currently checked out.");
        }
        if (req.getToBranchId() != null) {
            LibraryBranch targetBranch = libraryBranchRepository.findById(req.getToBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Target branch not found: " + req.getToBranchId()));
            copy.setBranch(targetBranch);
        }
        return bookCopyRepository.save(copy);
    }

    @Transactional(readOnly = true)
    public BookCopy findById(Long id) {
        return bookCopyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book copy not found with id: " + id));
    }
}
