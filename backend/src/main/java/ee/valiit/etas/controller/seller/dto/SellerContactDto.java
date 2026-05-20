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
    @NotBlank
    private String firstName;
    private String middleName;
    @NotBlank
    private String lastName;
    private String phone;
    @NotBlank
    private String email;
    private List<String> roles;
}
