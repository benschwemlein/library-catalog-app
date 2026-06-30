package com.example.catalog.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.catalog.repo.CatalogIdTypeRepository;

@RestController
@RequestMapping("/catalog/catalog-items33")
public class ModelMapperConfig {

    private final ModelMapper mapper;

    private CatalogIdTypeRepository catalogIdTypeRepository;

    @Autowired
    public ModelMapperConfig(CatalogIdTypeRepository catalogIdTypeRepository) {

        this.catalogIdTypeRepository = catalogIdTypeRepository;
        this.mapper = new ModelMapper();
        configureModelMapper();

        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    private void configureModelMapper() {

        /*mapper.typeMap(CatalogId.class, CatalogIdDTO.class).addMappings(mapper -> {
            mapper.map(src -> src.getValue(), CatalogIdDTO::setValue);
        });
        
        //mapper.typeMap(CatalogId.class, CatalogIdDTO.class).addMappings(mapper -> {
        //    mapper.map(src -> src.getType(), CatalogIdDTO::setValue);
        //  });
        
        mapper.typeMap(CatalogItem.class, CatalogItemResponseDTO.class).addMappings(mapper -> {
            mapper.map(src -> src.getCatalogIds(), CatalogItemResponseDTO::setCatalogIds);
        });*/
    }

}
/*@Configuration
public class ModelMapperConfig {

    private final ModelMapper modelMapper;

    private CatalogIdTypeRepository catalogIdTypeRepository;

    @Autowired
    public ModelMapperConfig(ModelMapper modelMapper) {
        this.modelMapper = new ModelMapper();
        configureModelMapper();
    }

    private void configureModelMapper() {
        modelMapper.addMappings(new PropertyMap<CatalogIdDTO, CatalogId>() {
            @Override
            protected void configure() {
                using(context -> catalogIdTypeRepository.findByName(((CatalogIdDTO) context.getSource()).getType()))
                        .map(source, destination.getType());
            }
        });

        // Additional ModelMapper configuration if needed
    }*/
