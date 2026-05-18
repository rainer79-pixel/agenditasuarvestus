package ee.valiit.etas.controller.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerContactResponseDto {
    private Integer contactId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private String email;
    private List<String> roles;
}
