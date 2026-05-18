package ee.valiit.etas.persistence.sellercontact;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SellerContactRepository extends JpaRepository<SellerContact, Integer> {
    @Query("select sc from SellerContact sc where sc.seller.id = :sellerId order by sc.lastName")
    List<SellerContact> findSellerContactsBy(Integer sellerId);
}
