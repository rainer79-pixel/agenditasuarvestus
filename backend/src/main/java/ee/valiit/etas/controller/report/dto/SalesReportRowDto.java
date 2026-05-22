package ee.valiit.etas.controller.report.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SalesReportRowDto {
    private String issuerName;       // issuer_name      — ei kasutata
    private String sellerName;       // seller_name       — ei kasutata
    private Integer orgId;           // seller_org_id     → seller.org_id
    private String departmentName;   // dept_name         → department_name
    private String departmentId;     // seller_dept_id    → department_id
    private String paymentChannel;   // payment_channel   → payment_channel
    private String productType;      // tyyp              → product_type
    private Integer transactionCount;// tehinguid         → transaction_count
    private BigDecimal salesAmount;  // summas            → sales_amount
    private BigDecimal priceAddSum;  // price_add_sum     — ei kasutata
    private BigDecimal feeSum;       // fee_sum           — ei kasutata
    private String period;           // a_date            → period (teisendatakse)
    private String buyerChannel;     // buyer_channel     — ei kasutata
    private String region;           // piirkond          → region
    private String liiniomanik;      // liiniomanik       — ei kasutata
}
