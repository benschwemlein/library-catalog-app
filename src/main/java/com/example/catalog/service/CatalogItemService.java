package com.example.catalog.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.catalog.model.CatalogItem;
import com.example.catalog.model.User;
import com.example.catalog.repo.CatalogItemRepository;
import com.example.catalog.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogItemService {

    @Autowired
    private final CatalogItemRepository catalogItemRepository;

    @Autowired
    private final UserRepository userRepository;

    @Transactional
    public CatalogItem createCatalogItem(CatalogItem catalogItem) {
        User user = userRepository.findByEmail(catalogItem.getCreatedBy().getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        catalogItem.setCreatedBy(user);
        return catalogItemRepository.save(catalogItem);
    }

    @Transactional(readOnly = true)
    public List<CatalogItem> getAllCatalogItems() {
        return catalogItemRepository.findAllCatalogItemsWithPotentialCheckouts();
    }

    //TODO This needs to return checkouts.
    @Transactional(readOnly = true)
    public CatalogItem getCatalogItemById(Long id) {
        return catalogItemRepository.findById(id).orElseThrow(() -> new RuntimeException("Catalog item not found")); // Custom exception should be used
    }

    @Transactional
    public CatalogItem updateCatalogItem(Long id, CatalogItem catalogItem) {
        CatalogItem existingCatalogItem = getCatalogItemById(id); // Will throw if not found
        existingCatalogItem.setTitle(catalogItem.getTitle());
        existingCatalogItem.setDescription(catalogItem.getDescription());
        existingCatalogItem.setCatalogIds(catalogItem.getCatalogIds());
        //  existingCatalogItem.setCreatedBy(catalogItem.getCreatedBy()); // Assumes you handle the user entity
        // Add more fields update if necessary

        return catalogItemRepository.save(existingCatalogItem);
    }

    @Transactional
    public void deleteCatalogItem(Long id) {
        catalogItemRepository.deleteById(id);
    }
}
