package ee.valiit.etas.controller.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponseDto {
    private Integer sellerId;
    private String companyName;
    private String period;
    private Long transactionCount;
    private BigDecimal salesAmount;
    private BigDecimal feeAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalFee;
}