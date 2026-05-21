package ee.valiit.etas.persistence.producttype;

import ee.valiit.etas.controller.producttype.dto.ProductTypeResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductTypeMapper {
    @Mapping(source = "id", target = "productTypeId")
    @Mapping(source = "productTypeName", target = "productTypeName")
    ProductTypeResponseDto toProductTypeResponseDto(ProductType productType);
    List<ProductTypeResponseDto> toProductTypeResponseDtos(List<ProductType> productTypes);
}
