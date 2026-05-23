package ee.valiit.etas.persistence.salesreportdetail;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SalesReportDetailRepository extends JpaRepository<SalesReportDetail, Integer> {
    @Modifying
    @Transactional
    @Query("delete from SalesReportDetail s where s.salesReport.id = :salesReportId")
    void deleteAllBy(Integer salesReportId);
}
