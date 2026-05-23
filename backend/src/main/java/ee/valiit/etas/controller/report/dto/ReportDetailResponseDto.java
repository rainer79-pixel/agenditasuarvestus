package ee.valiit.etas.controller.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetailResponseDto {
    private String productTypeName;
    private Long transactionCount;
    private BigDecimal salesAmount;
    private BigDecimal feePerTransaction;
    private BigDecimal feePercent;
    private BigDecimal calculatedFee;
    private BigDecimal vatAmount;
    private BigDecimal totalFee;
}
