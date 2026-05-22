package ee.valiit.etas.controller;

import ee.valiit.etas.persistence.CommissionCalculationService;
import ee.valiit.etas.persistence.view.CommissionCalculationViewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommissionCalculationController {

    private final CommissionCalculationService commissionCalculationService;

    @GetMapping("/commission-info-all")
    public List<CommissionCalculationViewDto> getCommissions() {
        return commissionCalculationService.getCommissions();
    }

}
