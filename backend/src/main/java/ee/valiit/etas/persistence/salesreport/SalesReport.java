package ee.valiit.etas.persistence.salesreport;

import ee.valiit.etas.persistence.seller.Seller;
import ee.valiit.etas.persistence.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sales_report", schema = "etas")
public class SalesReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Size(max = 10)
    @NotNull
    @Column(name = "period", nullable = false, length = 10)
    private String period;

    @NotNull
    @ColumnDefault("CURRENT_DATE")
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

}
