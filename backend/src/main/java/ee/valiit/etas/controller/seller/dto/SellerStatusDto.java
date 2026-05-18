package ee.valiit.etas.controller.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerStatusDto {
    @NotBlank
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "peab olema ACTIVE või INACTIVE")
    private String status;
}
