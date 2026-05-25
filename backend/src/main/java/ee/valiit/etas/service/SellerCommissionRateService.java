package ee.valiit.etas.service;

import ee.valiit.etas.controller.seller.dto.CommissionRateDto;
import ee.valiit.etas.controller.seller.dto.CommissionRateResponseDto;
import ee.valiit.etas.infrastructure.exception.ConflictException;
import ee.valiit.etas.infrastructure.exception.DataNotFoundException;
import ee.valiit.etas.persistence.commissioncalculation.CommissionCalculationRepository;
import ee.valiit.etas.persistence.commissionrate.CommissionRate;
import ee.valiit.etas.persistence.commissionrate.CommissionRateMapper;
import ee.valiit.etas.persistence.commissionrate.CommissionRateRepository;
import ee.valiit.etas.persistence.producttype.ProductType;
import ee.valiit.etas.persistence.producttype.ProductTypeRepository;
import ee.valiit.etas.persistence.seller.Seller;
import ee.valiit.etas.persistence.seller.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static ee.valiit.etas.infrastructure.error.ErrorResponse.*;

@Service
@RequiredArgsConstructor
public class SellerCommissionRateService {

    private final CommissionRateRepository commissionRateRepository;
    private final CommissionRateMapper commissionRateMapper;
    private final SellerRepository sellerRepository;
    private final ProductTypeRepository productTypeRepository;
    private final CommissionCalculationRepository commissionCalculationRepository;
    private final ValidationService validationService;

    public List<CommissionRateResponseDto> findSellerCommissionRates(Integer sellerId) {
        List<CommissionRate> commissionRates = commissionRateRepository.findCommissionRatesBy(sellerId);
        return commissionRateMapper.toCommissionRateResponseDtos(commissionRates);
    }

    @Transactional
    public void updateCommissionRate(Integer userId, Integer sellerId, Integer commissionRateId, CommissionRateDto commissionRateDto) {
        validationService.validateUserIsAdmin(userId);
        validationService.validateSellerExists(sellerId);
        CommissionRate commissionRate = commissionRateRepository.findById(commissionRateId)
                .orElseThrow(() -> new DataNotFoundException(COMMISSION_RATE_NOT_FOUND.getMessage(), COMMISSION_RATE_NOT_FOUND.getErrorCode()));
        ProductType productType = productTypeRepository.findById(commissionRateDto.getProductTypeId())
                .orElseThrow(() -> new DataNotFoundException(PRODUCT_TYPE_NOT_FOUND.getMessage(), PRODUCT_TYPE_NOT_FOUND.getErrorCode()));
        commissionRate.setProductType(productType);
        commissionRateMapper.updateCommissionRate(commissionRateDto, commissionRate);
        commissionRateRepository.save(commissionRate);
    }

    @Transactional
    public void deleteSellerCommissionRate(Integer userId, Integer sellerId, Integer commissionRateId) {
        validationService.validateUserIsAdmin(userId);
        validationService.validateSellerExists(sellerId);
        validateCommissionRateExists(commissionRateId);
        validateCommissionRateNotInUse(commissionRateId);
        commissionRateRepository.deleteById(commissionRateId);
    }

    @Transactional
    public void addSellerCommissionRate(Integer sellerId, Integer userId, CommissionRateDto commissionRateDto) {
        validationService.validateUserIsAdmin(userId);
        validationService.validateSellerExists(sellerId);
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new DataNotFoundException(SELLER_NOT_FOUND.getMessage(), SELLER_NOT_FOUND.getErrorCode()));
        ProductType productType = productTypeRepository.findById(commissionRateDto.getProductTypeId())
                .orElseThrow(() -> new DataNotFoundException(PRODUCT_TYPE_NOT_FOUND.getMessage(), PRODUCT_TYPE_NOT_FOUND.getErrorCode()));
        if (commissionRateRepository.commissionRateExistsBy(sellerId, commissionRateDto.getProductTypeId())) {
            throw new ConflictException(COMMISSION_RATE_ALREADY_EXISTS.getMessage(), COMMISSION_RATE_ALREADY_EXISTS.getErrorCode());
        }
        CommissionRate commissionRate = commissionRateMapper.toCommissionRate(commissionRateDto);
        commissionRate.setSeller(seller);
        commissionRate.setProductType(productType);
        commissionRateRepository.save(commissionRate);
    }

    private void validateCommissionRateExists(Integer commissionRateId) {
        if (!commissionRateRepository.existsById(commissionRateId)) {
            throw new DataNotFoundException(COMMISSION_RATE_NOT_FOUND.getMessage(), COMMISSION_RATE_NOT_FOUND.getErrorCode());
        }
    }

    private void validateCommissionRateNotInUse(Integer commissionRateId) {
        if (commissionCalculationRepository.commissionCalculationExistsBy(commissionRateId)) {
            throw new ConflictException(COMMISSION_RATE_IN_USE.getMessage(), COMMISSION_RATE_IN_USE.getErrorCode());
        }
    }
}