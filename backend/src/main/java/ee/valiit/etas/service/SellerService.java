package ee.valiit.etas.service;

import ee.valiit.etas.Status;
import ee.valiit.etas.controller.seller.dto.*;
import ee.valiit.etas.infrastructure.exception.ConflictException;
import ee.valiit.etas.infrastructure.exception.DataNotFoundException;
import ee.valiit.etas.persistence.seller.Seller;
import ee.valiit.etas.persistence.seller.SellerMapper;
import ee.valiit.etas.persistence.seller.SellerRepository;
import ee.valiit.etas.persistence.user.User;
import ee.valiit.etas.persistence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static ee.valiit.etas.infrastructure.error.ErrorResponse.*;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final SellerMapper sellerMapper;
    private final ValidationService validationService;

    public List<SellerDetailResponseDto> findSellers() {
        List<Seller> sellers = sellerRepository.findAllSellers();
        return sellerMapper.toSellerDetailResponseDtos(sellers);
    }

    public SellerDetailResponseDto findSeller(Integer sellerId) {
        Seller seller = getSeller(sellerId);
        return sellerMapper.toSellerDetailResponseDto(seller);
    }

    @Transactional
    public void addSeller(Integer userId, SellerDto sellerDto) {
        validationService.validateUserIsAdmin(userId);
        validateOrgIdIsAvailable(sellerDto.getOrgId());
        createAndSaveSeller(userId, sellerDto);
    }

    @Transactional
    public void updateSeller(Integer userId, Integer sellerId, SellerDto sellerDto) {
        validationService.validateUserIsAdmin(userId);
        Seller seller = getSeller(sellerId);
        sellerMapper.updateSeller(sellerDto, seller);
        sellerRepository.save(seller);
    }

    @Transactional
    public void updateSellerStatus(Integer userId, Integer sellerId, SellerStatusDto sellerStatusDto) {
        validationService.validateUserIsAdmin(userId);
        Seller seller = getSeller(sellerId);
        validateStatusChange(seller, sellerStatusDto.getStatus());
        seller.setStatus(Status.fromApiValue(sellerStatusDto.getStatus()).getCode());
        sellerRepository.save(seller);
    }

    private Seller getSeller(Integer sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new DataNotFoundException(SELLER_NOT_FOUND.getMessage(), SELLER_NOT_FOUND.getErrorCode()));
    }

    private void validateOrgIdIsAvailable(Integer orgId) {
        if (sellerRepository.existsByOrgId(orgId)) {
            throw new ConflictException(SELLER_ORG_ID_ALREADY_EXISTS.getMessage(), SELLER_ORG_ID_ALREADY_EXISTS.getErrorCode());
        }
    }

    private void createAndSaveSeller(Integer userId, SellerDto sellerDto) {
        User user = userRepository.findById(userId).orElseThrow();
        Seller seller = sellerMapper.toSeller(sellerDto);
        seller.setCreatedBy(user);
        seller.setCreatedAt(LocalDateTime.now());
        sellerRepository.save(seller);
    }

    private void validateStatusChange(Seller seller, String newStatus) {
        if (Status.SOFT_DELETED.getCode().equals(seller.getStatus()) && "INACTIVE".equals(newStatus)) {
            throw new ConflictException(SELLER_ALREADY_INACTIVE.getMessage(), SELLER_ALREADY_INACTIVE.getErrorCode());
        }
        if (Status.ACTIVE.getCode().equals(seller.getStatus()) && "ACTIVE".equals(newStatus)) {
            throw new ConflictException(SELLER_ALREADY_ACTIVE.getMessage(), SELLER_ALREADY_ACTIVE.getErrorCode());
        }
    }
}
