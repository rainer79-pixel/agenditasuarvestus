package ee.valiit.etas.persistence.view;

import ee.valiit.etas.controller.report.dto.ReportDetailResponseDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommissionCalculationViewMapper {

    CommissionCalculationViewDto toDto(CommissionCalculationView commissionCalculationView);

    List<CommissionCalculationViewDto> toCommissionCalculationViewDtos(List<CommissionCalculationView> commissionCalculationViews);

    @Mapping(source = "productTypeName", target = "productTypeName")
   @Mapping(source = "transactionCountSum", target = "transactionCount")
   @Mapping(source = "salesAmountSum", target = "salesAmount")
   @Mapping(source = "feePerTransaction", target = "feePerTransaction")
   @Mapping(source = "feePercent", target = "feePercent")
   @Mapping(source = "calculatedFee", target = "calculatedFee")
   @Mapping(source = "vatAmount", target = "vatAmount")
   @Mapping(source = "calculatedFeePlusVat", target = "totalFee")
   ReportDetailResponseDto toReportDetailResponseDto(CommissionCalculationView view);

    List<ReportDetailResponseDto> toReportDetailResponseDtos(List<CommissionCalculationView> views);
}