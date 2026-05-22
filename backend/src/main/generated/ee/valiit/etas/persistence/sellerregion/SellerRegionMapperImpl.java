package ee.valiit.etas.persistence.sellerregion;

import ee.valiit.etas.controller.seller.dto.SellerRegionDto;
import ee.valiit.etas.controller.seller.dto.SellerRegionResponseDto;
import ee.valiit.etas.persistence.region.Region;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-22T15:11:47+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (JetBrains s.r.o.)"
)
@Component
public class SellerRegionMapperImpl implements SellerRegionMapper {

    @Override
    public SellerRegionResponseDto toSellerRegionResponseDto(SellerRegion sellerRegion) {
        if ( sellerRegion == null ) {
            return null;
        }

        SellerRegionResponseDto sellerRegionResponseDto = new SellerRegionResponseDto();

        sellerRegionResponseDto.setRegionId( sellerRegion.getId() );
        sellerRegionResponseDto.setRegionName( sellerRegionRegionRegionName( sellerRegion ) );
        sellerRegionResponseDto.setSalesPointCount( sellerRegion.getSalesPointCount() );

        return sellerRegionResponseDto;
    }

    @Override
    public List<SellerRegionResponseDto> toSellerRegionResponseDtos(List<SellerRegion> sellerRegions) {
        if ( sellerRegions == null ) {
            return null;
        }

        List<SellerRegionResponseDto> list = new ArrayList<SellerRegionResponseDto>( sellerRegions.size() );
        for ( SellerRegion sellerRegion : sellerRegions ) {
            list.add( toSellerRegionResponseDto( sellerRegion ) );
        }

        return list;
    }

    @Override
    public SellerRegion toSellerRegion(SellerRegionDto sellerRegionDto) {
        if ( sellerRegionDto == null ) {
            return null;
        }

        SellerRegion sellerRegion = new SellerRegion();

        sellerRegion.setSalesPointCount( sellerRegionDto.getSalesPointCount() );

        return sellerRegion;
    }

    private String sellerRegionRegionRegionName(SellerRegion sellerRegion) {
        Region region = sellerRegion.getRegion();
        if ( region == null ) {
            return null;
        }
        return region.getRegionName();
    }
}
