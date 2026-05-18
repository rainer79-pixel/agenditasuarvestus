package ee.valiit.etas.persistence.seller;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SellerRepository extends JpaRepository<Seller, Integer> {

    @Query("select count(s) from Seller s where s.status = :status")
    int countSellersBy(@Param("status") String status);

    @Query("select s from Seller s order by s.companyName")
    List<Seller> findAllSellers();

    boolean existsByOrgId(Integer orgId);
}
