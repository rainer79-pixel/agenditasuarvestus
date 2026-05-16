package ee.valiit.etas.controller.login.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {
    @NotBlank(message = "Email on kohustuslik")
    private String email;
    @NotBlank(message = "Parool on kohustuslik")
    private String password;
}
