package ee.valiit.etas.persistence.commissioncalculation;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommissionCalculationRepository extends JpaRepository<CommissionCalculation, Integer> {

    @Query("select (count(c) > 0) from CommissionCalculation c where c.commissionRate.id = :commissionRateId")
    boolean commissionCalculationExistsBy(Integer commissionRateId);

    @Modifying
    @Transactional
    @Query("delete from CommissionCalculation c where c.salesReport.id = :salesReportId")
    void deleteAllBy(Integer salesReportId);
}
