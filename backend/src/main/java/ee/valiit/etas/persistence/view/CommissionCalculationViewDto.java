package ee.valiit.etas.persistence.view;

import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link CommissionCalculationView}
 */
@Value
public class CommissionCalculationViewDto implements Serializable {
    Long id;
    Integer salesReportId;
    Integer sellerId;
    Integer productTypeId;
    @Size(max = 255)
    String companyName;
    @Size(max = 100)
    String productTypeName;
    Long transactionCountSum;
    BigDecimal salesAmountSum;
    BigDecimal feeSum;
    BigDecimal feePerTransaction;
    BigDecimal feePercent;
    Boolean includesVat;
    BigDecimal calculatedFee;
    BigDecimal vatRate;
    BigDecimal vatAmount;
    BigDecimal calculatedFeePlusVat;
}