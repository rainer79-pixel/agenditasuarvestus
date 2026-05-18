package ee.valiit.etas.controller.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRateResponseDto {
    private Integer commissionRateId;
    private String productTypeName;
    private BigDecimal feePerTransaction;
    private BigDecimal feePercent;
    private Boolean includesVat;
    private String validFrom;
    private String validTo;
}
