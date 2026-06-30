package com.example.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.catalog.model.CatalogId;

public interface CatalogIdRepository extends JpaRepository<CatalogId, Long> {}
