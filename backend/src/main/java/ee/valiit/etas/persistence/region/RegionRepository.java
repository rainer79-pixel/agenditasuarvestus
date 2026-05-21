package ee.valiit.etas.persistence.region;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Integer> {
    @Query("select r from Region r order by r.sequenceNumber")
    List<Region> findAllRegions();
}
