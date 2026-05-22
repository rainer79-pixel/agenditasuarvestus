package ee.valiit.etas.persistence.salesreportdetail;

import ee.valiit.etas.controller.report.dto.SalesReportRowDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface SalesReportDetailMapper {

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "salesReport")
    @Mapping(ignore = true, target = "seller")
    @Mapping(ignore = true, target = "fee")
    @Mapping(ignore = true, target = "createdAt")
    SalesReportDetail toSalesReportDetail(SalesReportRowDto dto);

    List<SalesReportDetail> toSalesReportDetails(List<SalesReportRowDto> dtos);
}
