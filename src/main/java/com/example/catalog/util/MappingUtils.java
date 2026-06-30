package com.example.catalog.util;

import java.util.List;
import java.util.stream.Collectors;

import com.example.catalog.dto.CatalogIdDTO;
import com.example.catalog.dto.CatalogIdTypeDTO;
import com.example.catalog.dto.CatalogItemResponseDTO;
import com.example.catalog.model.CatalogId;
import com.example.catalog.model.CatalogItem;

public class MappingUtils {

    //TODO This shoudl be done by the model mapper, but it was not working.
    //There is an assumption on order of lists.
    public static List<CatalogItemResponseDTO> mapCatalogIds(List<CatalogItem> items,
            List<CatalogItemResponseDTO> catalogItemDTOs) {

        for(int i = 0; i < items.size(); i++) {
            CatalogItem item = items.get(i);
            CatalogItemResponseDTO dto = catalogItemDTOs.get(i);

            List<CatalogIdDTO> catalogIdDTOs = item.getCatalogIds().stream()
                    .map(catalogId -> convertToCatalogIdDTO(catalogId)).collect(Collectors.toList());

            dto.setCatalogIds(catalogIdDTOs);
        }

        return catalogItemDTOs;
    }

    private static CatalogIdDTO convertToCatalogIdDTO(CatalogId catalogId) {

        return CatalogIdDTO.builder().value(catalogId.getValue())
                .type(CatalogIdTypeDTO.builder().name(catalogId.getType().getName()).build()).build();
    }

}
