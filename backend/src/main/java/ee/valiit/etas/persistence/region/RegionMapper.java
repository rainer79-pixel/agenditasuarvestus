package ee.valiit.etas.persistence.region;

import ee.valiit.etas.controller.region.dto.RegionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface RegionMapper {
    @Mapping(source = "id", target = "regionId" )
    @Mapping(source = "regionName", target = "regionName")
    RegionResponseDto toRegionResponseDto (Region region);
    List<RegionResponseDto> toRegionResponseDtos(List<Region> regions);
}
