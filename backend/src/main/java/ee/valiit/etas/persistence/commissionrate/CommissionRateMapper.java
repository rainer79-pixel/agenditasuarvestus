package ee.valiit.etas.persistence.commissionrate;

import ee.valiit.etas.controller.seller.dto.CommissionRateDto;
import ee.valiit.etas.controller.seller.dto.CommissionRateResponseDto;
import org.mapstruct.*;
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

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(expression = "java(commissionRateDto.getValidFrom() != null ? LocalDate.parse(commissionRateDto.getValidFrom(), DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")) : null)",
            target = "validFrom")
    @Mapping(expression = "java(commissionRateDto.getValidTo() != null ? LocalDate.parse(commissionRateDto.getValidTo(), DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")) : null)", target
            = "validTo")
    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "productType")
    void updateCommissionRate(CommissionRateDto commissionRateDto, @MappingTarget CommissionRate commissionRate);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "seller")
    @Mapping(ignore = true, target = "productType")
    @Mapping(source = "feePerTransaction", target = "feePerTransaction")
    @Mapping(source = "feePercent", target = "feePercent")
    @Mapping(source = "includesVat", target = "includesVat")
    @Mapping(expression = "java(commissionRateDto.getValidFrom() != null ? LocalDate.parse(commissionRateDto.getValidFrom(), DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")) : null)",
            target = "validFrom")
    @Mapping(expression = "java(commissionRateDto.getValidTo() != null ? LocalDate.parse(commissionRateDto.getValidTo(), DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")) : null)", target
            = "validTo")
    CommissionRate toCommissionRate(CommissionRateDto commissionRateDto);
}
