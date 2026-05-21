package ee.valiit.etas.persistence.contactrole;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContactRoleRepository extends JpaRepository<ContactRole, Integer> {
    Optional<ContactRole> findByCode(String code);
}
