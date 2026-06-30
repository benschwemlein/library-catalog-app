package com.example.library.service;

import com.example.library.entity.Author;
import com.example.library.entity.Book;
import com.example.library.exception.BookNotFoundException;
import com.example.library.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    @Transactional(readOnly = true)
    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Author findById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Author not found with id: " + id));
    }

    public Author create(Author author) {
        return authorRepository.save(author);
    }

    public Author update(Long id, Author updated) {
        Author author = findById(id);
        author.setFirstName(updated.getFirstName());
        author.setLastName(updated.getLastName());
        author.setBio(updated.getBio());
        author.setBirthDate(updated.getBirthDate());
        author.setNationality(updated.getNationality());
        return authorRepository.save(author);
    }

    public void delete(Long id) {
        Author author = findById(id);
        authorRepository.delete(author);
    }

    @Transactional(readOnly = true)
    public List<Book> findBooksByAuthor(Long authorId) {
        Author author = findById(authorId);
        return new ArrayList<>(author.getBooks());
    }

    @Transactional(readOnly = true)
    public List<Author> searchAuthors(String name) {
        return authorRepository.findByFirstNameContainingOrLastNameContaining(name, name);
    }
}
