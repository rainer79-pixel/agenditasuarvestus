package ee.valiit.etas.persistence;

import ee.valiit.etas.persistence.view.CommissionCalculationView;
import ee.valiit.etas.persistence.view.CommissionCalculationViewDto;
import ee.valiit.etas.persistence.view.CommissionCalculationViewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommissionCalculationService {


    private final CommissionCalculationViewRepository commissionCalculationViewRepository;
    private final CommissionCalculationViewMapper commissionCalculationViewMapper;

    public List<CommissionCalculationViewDto> getCommissions() {
        List<CommissionCalculationView> commissionCalculationViewDtos = commissionCalculationViewRepository.findAll();
        return commissionCalculationViewMapper.toCommissionCalculationViewDtos(commissionCalculationViewDtos);
    }
}
