package ee.valiit.etas.service;

import ee.valiit.etas.controller.seller.dto.SellerRegionResponseDto;
import ee.valiit.etas.persistence.sellerregion.SellerRegion;
import ee.valiit.etas.persistence.sellerregion.SellerRegionMapper;
import ee.valiit.etas.persistence.sellerregion.SellerRegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerRegionService {
    private final SellerRegionRepository sellerRegionRepository;
    private final SellerRegionMapper sellerRegionMapper;

    public List<SellerRegionResponseDto> findSellerRegions(Integer sellerId) {
        List<SellerRegion> sellerRegions = sellerRegionRepository.findSellerRegionsBy(sellerId);
        return sellerRegionMapper.toSellerRegionResponseDtos(sellerRegions);
    }
}
