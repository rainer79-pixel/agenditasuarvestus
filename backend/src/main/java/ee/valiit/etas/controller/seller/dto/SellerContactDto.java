package ee.valiit.etas.controller.seller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerContactDto {
    @NotBlank(message = "Eesnimi on kohustuslik")
    private String firstName;
    private String middleName;
    @NotBlank(message = "Perekonnanimi on kohustuslik")
    private String lastName;
    private String phone;
    @NotBlank(message = "E-mail on kohustuslik")
    private String email;
    private List<String> roles;
}
