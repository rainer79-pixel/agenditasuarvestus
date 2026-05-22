package ee.valiit.etas.service;

import ee.valiit.etas.controller.producttype.dto.ProductTypeResponseDto;
import ee.valiit.etas.persistence.producttype.ProductType;
import ee.valiit.etas.persistence.producttype.ProductTypeMapper;
import ee.valiit.etas.persistence.producttype.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductTypeService {
    private final ProductTypeRepository productTypeRepository;
    private final ProductTypeMapper productTypeMapper;

    public List<ProductTypeResponseDto> getProductTypes() {
        List<ProductType> productTypes = productTypeRepository.findAll();
        return productTypeMapper.toProductTypeResponseDtos(productTypes);
    }
}
