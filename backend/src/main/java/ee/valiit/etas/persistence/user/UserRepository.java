package ee.valiit.etas.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("""
            select u from User u
            where upper(u.email) = upper(:email) and u.password = :password and u.userStatus = :userStatus""")
    Optional<User> findUserBy(@Param("email") String email, @Param("password") String password, @Param("userStatus") String userStatus);
}
