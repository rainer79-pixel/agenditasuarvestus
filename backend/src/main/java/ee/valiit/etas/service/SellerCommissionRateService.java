package ee.valiit.etas.service;

import ee.valiit.etas.controller.seller.dto.CommissionRateResponseDto;
import ee.valiit.etas.infrastructure.exception.ConflictException;
import ee.valiit.etas.infrastructure.exception.DataNotFoundException;
import ee.valiit.etas.infrastructure.exception.ForbiddenException;
import ee.valiit.etas.persistence.commissioncalculation.CommissionCalculationRepository;
import ee.valiit.etas.persistence.commissionrate.CommissionRate;
import ee.valiit.etas.persistence.commissionrate.CommissionRateMapper;
import ee.valiit.etas.persistence.commissionrate.CommissionRateRepository;
import ee.valiit.etas.persistence.seller.SellerRepository;
import ee.valiit.etas.persistence.user.User;
import ee.valiit.etas.persistence.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ee.valiit.etas.infrastructure.error.ErrorResponse.*;

@Service
@RequiredArgsConstructor
public class SellerCommissionRateService {
    private final CommissionRateRepository commissionRateRepository;
    private final CommissionRateMapper commissionRateMapper;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final CommissionCalculationRepository commissionCalculationRepository;


    public List<CommissionRateResponseDto> findSellerCommissionRates(Integer sellerId) {
        List<CommissionRate> commissionRates = commissionRateRepository.findCommissionRatesBy(sellerId);
        return commissionRateMapper.toCommissionRateResponseDtos(commissionRates);
    }

    @Transactional
    public void deleteSellerCommissionRate(Integer userId, Integer sellerId, Integer commissionRateId){
        validateUserIsAdmin(userId);
        validateSellerExists(sellerId);
        validateCommissionRateExists(commissionRateId);
        validateCommissionRateNotInUse(commissionRateId);
        commissionRateRepository.deleteById(commissionRateId);
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
