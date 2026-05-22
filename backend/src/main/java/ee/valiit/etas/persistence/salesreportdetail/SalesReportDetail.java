package ee.valiit.etas.persistence.salesreportdetail;

import ee.valiit.etas.persistence.salesreport.SalesReport;
import ee.valiit.etas.persistence.seller.Seller;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "sales_report_detail", schema = "etas")
public class SalesReportDetail {
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
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Size(max = 255)
    @NotNull
    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @Size(max = 50)
    @Column(name = "department_id", length = 50)
    private String departmentId;

    @Size(max = 100)
    @Column(name = "payment_channel", length = 100)
    private String paymentChannel;

    @Size(max = 100)
    @NotNull
    @Column(name = "product_type", nullable = false, length = 100)
    private String productType;

    @NotNull
    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount;

    @Column(name = "sales_amount", precision = 12, scale = 2)
    private BigDecimal salesAmount;
    @Column(name = "fee", precision = 12, scale = 2)
    private BigDecimal fee;
    @Size(max = 20)
    @NotNull
    @Column(name = "period", nullable = false, length = 20)
    private String period;
    @Size(max = 100)
    @Column(name = "region", length = 100)
    private String region;
    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


}