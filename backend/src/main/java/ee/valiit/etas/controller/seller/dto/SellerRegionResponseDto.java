package ee.valiit.etas.controller.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerRegionResponseDto {
    private Integer regionId;
    private String regionName;
    private Integer salesPointCount;
}
