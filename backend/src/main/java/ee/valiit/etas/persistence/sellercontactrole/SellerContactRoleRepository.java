package ee.valiit.etas.persistence.sellercontactrole;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SellerContactRoleRepository extends JpaRepository<SellerContactRole, Integer> {
    @Query("select scr.contactRole.code from SellerContactRole scr where scr.sellerContact.id = :contactId")
    List<String> findRoleCodesBy(Integer contactId);

    @Modifying
    @Transactional
    @Query("delete from SellerContactRole scr where scr.sellerContact.id = :contactId")
    void deleteAllBySellerContactId(Integer contactId);
}
