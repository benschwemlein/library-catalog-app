package com.example.library.controller;

import com.example.library.dto.BookCopyConditionUpdateDTO;
import com.example.library.dto.BookCopyTransferDTO;
import com.example.library.entity.BookCopy;
import com.example.library.repository.BookCopyRepository;
import com.example.library.service.BookCopyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/copies")
public class BookCopyController {

    @Autowired
    private BookCopyService bookCopyService;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @GetMapping
    public ResponseEntity<List<BookCopy>> getAllCopies() {
        return ResponseEntity.ok(bookCopyRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookCopy> getCopyById(@PathVariable Long id) {
        return ResponseEntity.ok(bookCopyService.findById(id));
    }

    @PostMapping
    public ResponseEntity<BookCopy> createCopy(@RequestBody Object bookCopyDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookCopyService.create(bookCopyDTO));
    }

    @PutMapping("/{id}/condition")
    public ResponseEntity<BookCopy> updateCondition(@PathVariable Long id, @RequestBody BookCopyConditionUpdateDTO update) {
        return ResponseEntity.ok(bookCopyService.updateCondition(id, update));
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<BookCopy> transferCopy(@PathVariable Long id, @RequestBody BookCopyTransferDTO transfer) {
        return ResponseEntity.ok(bookCopyService.transfer(id, transfer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCopy(@PathVariable Long id) {
        bookCopyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
