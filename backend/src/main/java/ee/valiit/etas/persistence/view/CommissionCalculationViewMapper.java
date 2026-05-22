package ee.valiit.etas.persistence.view;

import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommissionCalculationViewMapper {

    CommissionCalculationViewDto toDto(CommissionCalculationView commissionCalculationView);


   List<CommissionCalculationViewDto> toCommissionCalculationViewDtos(List <CommissionCalculationView> commissionCalculationViews);

}