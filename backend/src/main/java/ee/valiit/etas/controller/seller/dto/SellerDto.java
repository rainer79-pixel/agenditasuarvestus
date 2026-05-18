package ee.valiit.etas.controller.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class SellerDto {
    @NotBlank
    private String companyName;
    @NotNull
    private Integer orgId;
    private String contractStart;
    private String contractEnd;
    private String notes;
}
