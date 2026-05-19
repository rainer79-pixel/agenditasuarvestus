package ee.valiit.etas.service;

import ee.valiit.etas.controller.seller.dto.CommissionRateResponseDto;
import ee.valiit.etas.persistence.commissionrate.CommissionRate;
import ee.valiit.etas.persistence.commissionrate.CommissionRateMapper;
import ee.valiit.etas.persistence.commissionrate.CommissionRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerCommissionRateService {
    private final CommissionRateRepository commissionRateRepository;
    private final CommissionRateMapper commissionRateMapper;

    public List<CommissionRateResponseDto> findSellerCommissionRates(Integer sellerId) {
        List<CommissionRate> commissionRates = commissionRateRepository.findCommissionRatesBy(sellerId);
        return commissionRateMapper.toCommissionRateResponseDtos(commissionRates);
    }
}
