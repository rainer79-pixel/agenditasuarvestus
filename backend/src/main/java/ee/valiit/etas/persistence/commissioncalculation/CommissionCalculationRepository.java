package ee.valiit.etas.persistence.commissioncalculation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommissionCalculationRepository extends JpaRepository<CommissionCalculation, Integer> {

    @Query("select (count(c) > 0) from CommissionCalculation c where c.commissionRate.id = :commissionRateId")
    boolean commissionCalculationExistsBy(Integer commissionRateId);
}
