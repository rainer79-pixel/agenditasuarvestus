package ee.valiit.etas.persistence.commissioncalculation;

import ee.valiit.etas.persistence.commissionrate.CommissionRate;
import ee.valiit.etas.persistence.salesreport.SalesReport;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "commission_calculation", schema = "etas")
public class CommissionCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_report_id", nullable = false)
    private SalesReport salesReport;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commission_rate_id", nullable = false)
    private CommissionRate commissionRate;

    @NotNull
    @Column(name = "calculated_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal calculatedFee;

    @NotNull
    @Column(name = "vat_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal vatAmount;

    @NotNull
    @Column(name = "calculated_fee_plus_vat", nullable = false, precision = 12, scale = 2)
    private BigDecimal calculatedFeePlusVat;

    @NotNull
    @ColumnDefault("CURRENT_DATE")
    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;

}
