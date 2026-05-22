package ee.valiit.etas.persistence.producttype;

import ee.valiit.etas.controller.producttype.dto.ProductTypeResponseDto;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-22T11:42:19+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (JetBrains s.r.o.)"
)
@Component
public class ProductTypeMapperImpl implements ProductTypeMapper {

    @Override
    public ProductTypeResponseDto toProductTypeResponseDto(ProductType productType) {
        if ( productType == null ) {
            return null;
        }

        ProductTypeResponseDto productTypeResponseDto = new ProductTypeResponseDto();

        productTypeResponseDto.setProductTypeId( productType.getId() );
        productTypeResponseDto.setProductTypeName( productType.getProductTypeName() );

        return productTypeResponseDto;
    }

    @Override
    public List<ProductTypeResponseDto> toProductTypeResponseDtos(List<ProductType> productTypes) {
        if ( productTypes == null ) {
            return null;
        }

        List<ProductTypeResponseDto> list = new ArrayList<ProductTypeResponseDto>( productTypes.size() );
        for ( ProductType productType : productTypes ) {
            list.add( toProductTypeResponseDto( productType ) );
        }

        return list;
    }
}
