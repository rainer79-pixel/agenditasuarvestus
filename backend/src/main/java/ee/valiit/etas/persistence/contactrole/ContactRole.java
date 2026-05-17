package ee.valiit.etas.persistence.contactrole;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "role", schema = "etas")
public class ContactRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 5)
    @NotNull
    @Column(name = "code", nullable = false, length = 5)
    private String code;

    @Size(max = 100)
    @NotNull
    @Column(name = "seller_role_name", nullable = false, length = 100)
    private String sellerRoleName;

}
