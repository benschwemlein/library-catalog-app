package com.example.library.service;

import com.example.library.entity.Genre;
import com.example.library.exception.BookNotFoundException;
import com.example.library.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public List<Genre> findAll() {
        return genreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Genre findById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Genre not found with id: " + id));
    }

    public Genre create(Genre genre) {
        return genreRepository.save(genre);
    }

    public Genre update(Long id, Genre updated) {
        Genre genre = findById(id);
        genre.setName(updated.getName());
        genre.setDescription(updated.getDescription());
        return genreRepository.save(genre);
    }

    public void delete(Long id) {
        Genre genre = findById(id);
        genreRepository.delete(genre);
    }
}
