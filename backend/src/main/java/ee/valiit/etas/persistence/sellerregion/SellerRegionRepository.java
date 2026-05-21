package ee.valiit.etas.persistence.sellerregion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SellerRegionRepository extends JpaRepository<SellerRegion, Integer> {

    @Query("select sr from SellerRegion sr join fetch sr.region where sr.seller.id = :sellerId order by sr.region.sequenceNumber")
    List<SellerRegion> findSellerRegionsBy(Integer sellerId);

    @Query("select (count(s) > 0) from SellerRegion s where s.seller.id = :sellerId and s.region.id = :regionId")
    boolean sellerRegionExistsBy(Integer sellerId, Integer regionId);
}

