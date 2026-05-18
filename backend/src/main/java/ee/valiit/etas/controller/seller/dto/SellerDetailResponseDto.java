package ee.valiit.etas.controller.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerDetailResponseDto {
    private Integer sellerId;
    private String companyName;
    private Integer orgId;
    private String status;
    private String contractStart;
    private String contractEnd;
    private String notes;
}
