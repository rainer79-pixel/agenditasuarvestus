package ee.valiit.etas.persistence.commissionrate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommissionRateRepository extends JpaRepository<CommissionRate, Integer> {
    @Query("select cr from CommissionRate cr join fetch cr.productType where cr.seller.id = :sellerId order by cr.validFrom")
    List<CommissionRate> findCommissionRatesBy(Integer sellerId);
}
