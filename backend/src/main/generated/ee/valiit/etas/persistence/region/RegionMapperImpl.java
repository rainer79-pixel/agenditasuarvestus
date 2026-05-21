package ee.valiit.etas.persistence.region;

import ee.valiit.etas.controller.region.dto.RegionResponseDto;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-21T15:25:32+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (JetBrains s.r.o.)"
)
@Component
public class RegionMapperImpl implements RegionMapper {

    @Override
    public RegionResponseDto toRegionResponseDto(Region region) {
        if ( region == null ) {
            return null;
        }

        RegionResponseDto regionResponseDto = new RegionResponseDto();

        regionResponseDto.setRegionId( region.getId() );
        regionResponseDto.setRegionName( region.getRegionName() );

        return regionResponseDto;
    }

    @Override
    public List<RegionResponseDto> toRegionResponseDtos(List<Region> regions) {
        if ( regions == null ) {
            return null;
        }

        List<RegionResponseDto> list = new ArrayList<RegionResponseDto>( regions.size() );
        for ( Region region : regions ) {
            list.add( toRegionResponseDto( region ) );
        }

        return list;
    }
}
