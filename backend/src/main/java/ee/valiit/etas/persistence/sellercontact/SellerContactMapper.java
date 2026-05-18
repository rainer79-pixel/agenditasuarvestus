package ee.valiit.etas.persistence.sellercontact;

import ee.valiit.etas.controller.seller.dto.SellerContactResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)

public interface SellerContactMapper {
    @Mapping(source = "id", target = "contactId")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "middleName", target = "middleName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "email", target = "email")
    SellerContactResponseDto toSellerContactResponseDto(SellerContact sellerContact);
}
