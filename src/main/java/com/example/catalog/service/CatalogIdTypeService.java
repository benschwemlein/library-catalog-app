package com.example.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.catalog.model.CatalogIdType;
import com.example.catalog.repo.CatalogIdTypeRepository;

@Service
public class CatalogIdTypeService {

    private final CatalogIdTypeRepository catalogIdTypeRepository;

    public CatalogIdTypeService(CatalogIdTypeRepository catalogIdTypeRepository) {
        this.catalogIdTypeRepository = catalogIdTypeRepository;
    }

    public List<CatalogIdType> getAllCatalogIdTypes() {
        return catalogIdTypeRepository.findAll();
    }
}
