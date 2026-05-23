package ee.valiit.etas.persistence.view;

import ee.valiit.etas.persistence.salesreport.SalesReport;
import jakarta.persistence.*;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Getter
@Entity
@Immutable
@Table(name = "commission_calculation_view", schema = "etas")
public class CommissionCalculationView {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "sales_report_id")
    private Integer salesReportId;

    @Column(name = "seller_id")
    private Integer sellerId;

    @Column(name = "product_type_id")
    private Integer productTypeId;

    @Size(max = 255)
    @Column(name = "company_name")
    private String companyName;

    @Size(max = 100)
    @Column(name = "product_type_name", length = 100)
    private String productTypeName;

    @Column(name = "transaction_count_sum")
    private Long transactionCountSum;

    @Column(name = "sales_amount_sum")
    private BigDecimal salesAmountSum;

    @Column(name = "fee_sum")
    private BigDecimal feeSum;

    @Column(name = "commission_rate_id")
    private Integer commissionRateId;

    @Column(name = "fee_per_transaction", precision = 10, scale = 4)
    private BigDecimal feePerTransaction;

    @Column(name = "fee_percent", precision = 5, scale = 2)
    private BigDecimal feePercent;

    @Column(name = "includes_vat")
    private Boolean includesVat;

    @Column(name = "calculated_fee")
    private BigDecimal calculatedFee;

    @Column(name = "vat_rate", precision = 5, scale = 2)
    private BigDecimal vatRate;

    @Column(name = "vat_amount")
    private BigDecimal vatAmount;

    @Column(name = "calculated_fee_plus_vat")
    private BigDecimal calculatedFeePlusVat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_report_id", insertable = false, updatable = false)
    private SalesReport salesReport;
}