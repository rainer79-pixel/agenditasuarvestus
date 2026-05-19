package ee.valiit.etas.service;

import ee.valiit.etas.controller.seller.dto.SellerRegionResponseDto;
import ee.valiit.etas.persistence.seller.SellerRepository;
import ee.valiit.etas.persistence.sellerregion.SellerRegion;
import ee.valiit.etas.persistence.sellerregion.SellerRegionMapper;
import ee.valiit.etas.persistence.sellerregion.SellerRegionRepository;
import ee.valiit.etas.persistence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ee.valiit.etas.infrastructure.exception.DataNotFoundException;
import ee.valiit.etas.infrastructure.exception.ForbiddenException;
import ee.valiit.etas.persistence.user.User;
import ee.valiit.etas.controller.seller.dto.SellerRegionDto;

import static ee.valiit.etas.infrastructure.error.ErrorResponse.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerRegionService {
    private final SellerRegionRepository sellerRegionRepository;
    private final SellerRegionMapper sellerRegionMapper;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;


    public List<SellerRegionResponseDto> findSellerRegions(Integer sellerId) {
        List<SellerRegion> sellerRegions = sellerRegionRepository.findSellerRegionsBy(sellerId);
        return sellerRegionMapper.toSellerRegionResponseDtos(sellerRegions);
    }
    @Transactional
    public void updateSellerRegion(Integer userId, Integer sellerId, Integer regionId, SellerRegionDto sellerRegionDto) {
        validateUserIsAdmin(userId);
        validateSellerExists(sellerId);
        SellerRegion sellerRegion = getSellerRegion(regionId);
        sellerRegion.setSalesPointCount(sellerRegionDto.getSalesPointCount());
        sellerRegionRepository.save(sellerRegion);
    }
    private void validateUserIsAdmin(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND.getMessage(), USER_NOT_FOUND.getErrorCode()));
        if (!"A".equals(user.getUserRole())) {
            throw new ForbiddenException(ACCESS_DENIED.getMessage(), ACCESS_DENIED.getErrorCode());
        }
    }

    private void validateSellerExists(Integer sellerId) {
        if (!sellerRepository.existsById(sellerId)) {
            throw new DataNotFoundException(SELLER_NOT_FOUND.getMessage(), SELLER_NOT_FOUND.getErrorCode());
        }
    }

    private SellerRegion getSellerRegion(Integer regionId) {
        return sellerRegionRepository.findById(regionId)
                .orElseThrow(() -> new DataNotFoundException(SELLER_REGION_NOT_FOUND.getMessage(), SELLER_REGION_NOT_FOUND.getErrorCode()));
    }
}
