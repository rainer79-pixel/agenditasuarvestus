package ee.valiit.etas.persistence.sellercontactrole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SellerContactRoleRepository extends JpaRepository<SellerContactRole, Integer> {
    @Query("select scr.contactRole.code from SellerContactRole scr where scr.sellerContact.id = :contactId")
    List<String> findRoleCodesBy(Integer contactId);
}
