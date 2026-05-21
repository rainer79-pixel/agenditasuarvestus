package ee.valiit.etas.persistence.sellerregion;

import ee.valiit.etas.persistence.region.Region;
import ee.valiit.etas.persistence.seller.Seller;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "seller_region", schema = "etas")
public class SellerRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @NotNull
    @Column(name = "sales_point_count", nullable = false)
    private Integer salesPointCount;
}
