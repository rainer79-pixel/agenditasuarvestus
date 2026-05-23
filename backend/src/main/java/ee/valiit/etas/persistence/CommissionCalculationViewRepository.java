package ee.valiit.etas.persistence;

import ee.valiit.etas.persistence.view.CommissionCalculationView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommissionCalculationViewRepository extends JpaRepository<CommissionCalculationView, Long> {

    List<CommissionCalculationView> findBySalesReportId(Integer salesReportId);
    List<CommissionCalculationView> findBySalesReportIdAndSellerId(Integer salesReportId, Integer sellerId);
}