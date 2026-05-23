package ee.valiit.etas.persistence.salesreport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.Optional;

public interface SalesReportRepository extends JpaRepository<SalesReport, Integer> {
    @Query("select max(s.createdAt) from SalesReport s")
    Optional<LocalDate> findLastImportDate();

    Optional<SalesReport> findByPeriod(String period);

    @Query("select (count(s) > 0) from SalesReport s where s.period = :period")
    boolean existsByPeriod(String period);

}
