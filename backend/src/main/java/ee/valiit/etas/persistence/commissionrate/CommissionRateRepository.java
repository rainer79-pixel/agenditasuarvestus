package ee.valiit.etas.persistence.commissionrate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CommissionRateRepository extends JpaRepository<CommissionRate, Integer> {
    @Query("select cr from CommissionRate cr join fetch cr.productType where cr.seller.id = :sellerId order by cr.validFrom")
    List<CommissionRate> findCommissionRatesBy(Integer sellerId);

    @Query("""
            select (count(c) > 0) from CommissionRate c
            where c.seller.id = :sellerId and c.productType.id = :productTypeId and c.validTo is null""")
    boolean commissionRateExistsBy(Integer sellerId, Integer productTypeId);
}
