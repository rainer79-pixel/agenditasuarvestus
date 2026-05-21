package ee.valiit.etas.controller.producttype.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeResponseDto {
    private Integer productTypeId;
    private String productTypeName;
}
