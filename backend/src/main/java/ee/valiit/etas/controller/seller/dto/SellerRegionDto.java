package ee.valiit.etas.controller.seller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerRegionDto {

    @NotNull
    private Integer regionId;

    @NotNull
    @Min(0)
    private Integer salesPointCount;
}
