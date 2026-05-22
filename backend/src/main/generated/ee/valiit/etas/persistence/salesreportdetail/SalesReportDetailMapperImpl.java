package ee.valiit.etas.persistence.salesreportdetail;

import ee.valiit.etas.controller.report.dto.SalesReportRowDto;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-22T15:28:08+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (JetBrains s.r.o.)"
)
@Component
public class SalesReportDetailMapperImpl implements SalesReportDetailMapper {

    @Override
    public SalesReportDetail toSalesReportDetail(SalesReportRowDto dto) {
        if ( dto == null ) {
            return null;
        }

        SalesReportDetail salesReportDetail = new SalesReportDetail();

        salesReportDetail.setDepartmentName( dto.getDepartmentName() );
        salesReportDetail.setDepartmentId( dto.getDepartmentId() );
        salesReportDetail.setPaymentChannel( dto.getPaymentChannel() );
        salesReportDetail.setProductType( dto.getProductType() );
        salesReportDetail.setTransactionCount( dto.getTransactionCount() );
        salesReportDetail.setSalesAmount( dto.getSalesAmount() );
        salesReportDetail.setPeriod( dto.getPeriod() );
        salesReportDetail.setRegion( dto.getRegion() );

        return salesReportDetail;
    }

    @Override
    public List<SalesReportDetail> toSalesReportDetails(List<SalesReportRowDto> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<SalesReportDetail> list = new ArrayList<SalesReportDetail>( dtos.size() );
        for ( SalesReportRowDto salesReportRowDto : dtos ) {
            list.add( toSalesReportDetail( salesReportRowDto ) );
        }

        return list;
    }
}
