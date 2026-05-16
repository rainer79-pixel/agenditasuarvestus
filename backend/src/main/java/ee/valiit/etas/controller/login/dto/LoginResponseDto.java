package ee.valiit.etas.controller.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private Integer userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String role;
}