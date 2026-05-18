package ee.valiit.etas.persistence.commissionrate;


import ee.valiit.etas.controller.seller.dto.CommissionRateResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        imports = {LocalDate.class, DateTimeFormatter.class})
public interface CommissionRateMapper {

    @Mapping(source = "id", target = "commissionRateId")
    @Mapping(source = "productType.productTypeName", target = "productTypeName")
    @Mapping(source = "feePerTransaction", target = "feePerTransaction")
    @Mapping(source = "feePercent", target = "feePercent")
    @Mapping(source = "includesVat", target = "includesVat")
    @Mapping(expression = "java(commissionRate.getValidFrom() != null ? commissionRate.getValidFrom().format(DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")) : null)", target =
            "validFrom")
    @Mapping(expression = "java(commissionRate.getValidTo() != null ? commissionRate.getValidTo().format(DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")) : null)", target =
            "validTo")
    CommissionRateResponseDto toCommissionRateResponseDto(CommissionRate commissionRate);

    List<CommissionRateResponseDto> toCommissionRateResponseDtos(List<CommissionRate> commissionRates);
}
