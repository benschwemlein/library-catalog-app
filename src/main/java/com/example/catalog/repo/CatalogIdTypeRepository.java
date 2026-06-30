package com.example.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.catalog.model.CatalogIdType;

public interface CatalogIdTypeRepository extends JpaRepository<CatalogIdType, String> {

    CatalogIdType findByName(String type);
}
