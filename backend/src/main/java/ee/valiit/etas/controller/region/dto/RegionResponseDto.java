package ee.valiit.etas.controller.region.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionResponseDto {
    private Integer regionId;
    private String regionName;
}
