package ee.valiit.etas.controller.seller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRateDto {
    private Integer productTypeId;
    private BigDecimal feePerTransaction;
    private BigDecimal feePercent;
    @NotNull
    private Boolean includesVat;
    @NotNull
    private String validFrom;
    private String validTo;
}