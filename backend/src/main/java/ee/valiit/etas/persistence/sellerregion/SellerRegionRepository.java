package ee.valiit.etas.persistence.sellerregion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SellerRegionRepository extends JpaRepository<SellerRegion, Integer> {

    @Query("select sr from SellerRegion sr join fetch sr.region where sr.seller.id = :sellerId order by sr.region.sequenceNumber")
    List<SellerRegion> findSellerRegionsBy(Integer sellerId);
}

