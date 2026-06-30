package com.example.catalog.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.catalog.model.CatalogItem;

public interface CatalogItemRepository extends JpaRepository<CatalogItem, Long> {

    @Query("SELECT ci, co FROM CatalogItem ci LEFT JOIN ci.checkouts co ON co.checkedOut = TRUE")
    List<CatalogItem> findAllCatalogItemsWithPotentialCheckouts();

}
