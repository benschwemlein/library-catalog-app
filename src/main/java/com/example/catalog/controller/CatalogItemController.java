package com.example.catalog.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.catalog.dto.CatalogItemCheckoutRepsonseDTO;
import com.example.catalog.dto.CatalogItemRequestDTO;
import com.example.catalog.dto.CatalogItemResponseDTO;
import com.example.catalog.model.CatalogItem;
import com.example.catalog.model.Checkout;
import com.example.catalog.service.CatalogItemService;
import com.example.catalog.util.MappingUtils;

@RestController
@RequestMapping("/catalog/catalog-items")
public class CatalogItemController {

    private final CatalogItemService catalogItemService;

    private final ModelMapper mapper;

    @Autowired
    public CatalogItemController(CatalogItemService catalogItemService) {

        this.catalogItemService = catalogItemService;
        this.mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    @PostMapping
    public ResponseEntity<CatalogItemRequestDTO> createCatalogItem(@RequestBody CatalogItemRequestDTO catalogItemDTO) {

        CatalogItem catalogItem = mapper.map(catalogItemDTO, CatalogItem.class);

        CatalogItem createdItem = catalogItemService.createCatalogItem(catalogItem);
        CatalogItemRequestDTO catalogItemDto = mapper.map(createdItem, CatalogItemRequestDTO.class);
        return new ResponseEntity<>(catalogItemDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CatalogItemResponseDTO>> getAllCatalogItems() {
        List<CatalogItem> items = catalogItemService.getAllCatalogItems();

        List<CatalogItemResponseDTO> catalogItemDTOs = items.stream().map(this::convertToCatalogItemResponseDTO)
                .collect(Collectors.toList());
        MappingUtils.mapCatalogIds(items, catalogItemDTOs);

        return new ResponseEntity<>(catalogItemDTOs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogItemRequestDTO> getCatalogItemById(@PathVariable("id") Long id) {
        CatalogItem item = catalogItemService.getCatalogItemById(id);
        CatalogItemRequestDTO catalogItemDto = mapper.map(item, CatalogItemRequestDTO.class);
        return new ResponseEntity<>(catalogItemDto, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatalogItemRequestDTO> updateCatalogItem(@PathVariable("id") Long id,
            @RequestBody CatalogItemRequestDTO catalogItemDTO) {

        CatalogItem catalogItem = mapper.map(catalogItemDTO, CatalogItem.class);
        CatalogItem updatedItem = catalogItemService.updateCatalogItem(id, catalogItem);
        CatalogItemRequestDTO catalogItemDto = mapper.map(updatedItem, CatalogItemRequestDTO.class);
        return new ResponseEntity<>(catalogItemDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCatalogItem(@PathVariable("id") Long id) {
        catalogItemService.deleteCatalogItem(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private CatalogItemResponseDTO convertToCatalogItemResponseDTO(CatalogItem entity) {
        // Presumably you have a mapper to handle basic properties
        CatalogItemResponseDTO dto = mapper.map(entity, CatalogItemResponseDTO.class);

        // Set the first Checkout if available
        Checkout firstCheckout = entity.getCheckouts().stream().findFirst() // Get the first Checkout if it exists
                .orElse(null); // or return null if none are present

        if(firstCheckout != null) {
            // Map the first Checkout entity to your Checkout DTO
            CatalogItemCheckoutRepsonseDTO checkoutDTO = mapper.map(firstCheckout,
                                                                    CatalogItemCheckoutRepsonseDTO.class);
            dto.setCheckout(checkoutDTO);
        }

        return dto;
    }
}
