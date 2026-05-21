package ee.valiit.etas.service;

import ee.valiit.etas.controller.region.dto.RegionResponseDto;
import ee.valiit.etas.persistence.region.Region;
import ee.valiit.etas.persistence.region.RegionMapper;
import ee.valiit.etas.persistence.region.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {
    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    public List<RegionResponseDto> getRegions() {
        List<Region> regions = regionRepository.findAllRegions();
        return regionMapper.toRegionResponseDtos(regions);
    }
}
