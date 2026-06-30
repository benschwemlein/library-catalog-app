package com.example.catalog.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.catalog.dto.CatalogIdTypeDTO;
import com.example.catalog.model.CatalogIdType;
import com.example.catalog.service.CatalogIdTypeService;

@RestController
@RequestMapping("/catalog/catalog-id-types")
public class CatalogIdTypeController {

    private final CatalogIdTypeService catalogIdTypeService;
    private final ModelMapper modelMapper;

    public CatalogIdTypeController(CatalogIdTypeService catalogIdTypeService) {
        this.catalogIdTypeService = catalogIdTypeService;
        this.modelMapper = new ModelMapper();
    }

    @GetMapping
    public ResponseEntity<List<CatalogIdTypeDTO>> getAllCatalogIdTypes() {
        List<CatalogIdType> catalogIdTypes = catalogIdTypeService.getAllCatalogIdTypes();
        List<CatalogIdTypeDTO> catalogIdTypeDTOs = catalogIdTypes.stream()
                .map(catalogIdType -> modelMapper.map(catalogIdType, CatalogIdTypeDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(catalogIdTypeDTOs);
    }
}
