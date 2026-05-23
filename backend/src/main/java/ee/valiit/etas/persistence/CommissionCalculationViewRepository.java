package ee.valiit.etas.persistence;

import ee.valiit.etas.controller.report.dto.ReportResponseDto;
import ee.valiit.etas.persistence.view.CommissionCalculationView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommissionCalculationViewRepository extends JpaRepository<CommissionCalculationView, Long> {

    List<CommissionCalculationView> findBySalesReportId(Integer salesReportId);
    List<CommissionCalculationView> findBySalesReportIdAndSellerId(Integer salesReportId, Integer sellerId);

    @Query("select new ee.valiit.etas.controller.report.dto.ReportResponseDto(" +
           "    ccv.sellerId, ccv.companyName, ccv.salesReport.period," +
           "    SUM(ccv.transactionCountSum), SUM(ccv.salesAmountSum)," +
           "    SUM(ccv.calculatedFee), SUM(ccv.vatAmount), SUM(ccv.calculatedFeePlusVat)" +
           ") " +
           "from CommissionCalculationView ccv " +
           "where (:periodFrom is null or ccv.salesReport.period >= :periodFrom) " +
           "  and (:periodTo is null or ccv.salesReport.period <= :periodTo) " +
           "  and (:sellerId is null or ccv.sellerId = :sellerId) " +
           "group by ccv.sellerId, ccv.companyName, ccv.salesReport.period " +
           "order by ccv.companyName, ccv.salesReport.period")
    List<ReportResponseDto> findReports(
            @Param("periodFrom") String periodFrom,
            @Param("periodTo") String periodTo,
            @Param("sellerId") Integer sellerId);
}