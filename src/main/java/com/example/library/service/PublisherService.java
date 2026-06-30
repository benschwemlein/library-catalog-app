package com.example.library.service;

import com.example.library.entity.Publisher;
import com.example.library.exception.BookNotFoundException;
import com.example.library.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PublisherService {

    @Autowired
    private PublisherRepository publisherRepository;

    @Transactional(readOnly = true)
    public List<Publisher> findAll() {
        return publisherRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Publisher findById(Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Publisher not found with id: " + id));
    }

    public Publisher create(Publisher publisher) {
        return publisherRepository.save(publisher);
    }

    public Publisher update(Long id, Publisher updated) {
        Publisher publisher = findById(id);
        publisher.setName(updated.getName());
        publisher.setAddress(updated.getAddress());
        publisher.setWebsite(updated.getWebsite());
        return publisherRepository.save(publisher);
    }

    public void delete(Long id) {
        Publisher publisher = findById(id);
        publisherRepository.delete(publisher);
    }
}
