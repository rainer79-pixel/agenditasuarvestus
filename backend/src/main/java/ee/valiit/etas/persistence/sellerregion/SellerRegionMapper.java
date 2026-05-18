package ee.valiit.etas.persistence.sellerregion;

import ee.valiit.etas.controller.seller.dto.SellerRegionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)

public interface SellerRegionMapper {
    @Mapping(source = "id", target = "regionId")
    @Mapping(source = "region.regionName", target = "regionName")
    @Mapping(source = "salesPointCount", target = "salesPointCount")
    SellerRegionResponseDto toSellerRegionResponseDto(SellerRegion sellerRegion);

    List<SellerRegionResponseDto> toSellerRegionResponseDtos(List<SellerRegion> sellerRegions);
}
