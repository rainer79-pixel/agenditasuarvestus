package ee.valiit.etas.persistence.seller;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SellerRepository extends JpaRepository<Seller, Integer> {

    @Query("select count(s) from Seller s where s.status = :status")
    int countSellersBy(@Param("status") String status);
}
