package ee.valiit.etas.persistence.seller;

import ee.valiit.etas.Status;
import ee.valiit.etas.controller.seller.dto.SellerDetailResponseDto;
import ee.valiit.etas.controller.seller.dto.SellerDto;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        imports = {Status.class, LocalDate.class, DateTimeFormatter.class})
public interface SellerMapper {

    @Mapping(source = "id", target = "sellerId")
    @Mapping(source = "companyName", target = "companyName")
    @Mapping(source = "orgId", target = "orgId")
    @Mapping(source = "notes", target = "notes")
    @Mapping(expression = "java(Status.toApiValue(seller.getStatus()))", target = "status")
    @Mapping(expression = "java(seller.getContractStart() != null ? seller.getContractStart().format(DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")) : null)", target =
            "contractStart")
    @Mapping(expression = "java(seller.getContractEnd() != null ? seller.getContractEnd().format(DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")) : null)", target = "contractEnd")
    SellerDetailResponseDto toSellerDetailResponseDto(Seller seller);

    List<SellerDetailResponseDto> toSellerDetailResponseDtos(List<Seller> sellers);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "createdBy")
    @Mapping(ignore = true, target = "createdAt")
    @Mapping(source = "companyName", target = "companyName")
    @Mapping(source = "orgId", target = "orgId")
    @Mapping(source = "notes", target = "notes")
    @Mapping(expression = "java(Status.ACTIVE.getCode())", target = "status")
    @Mapping(expression = "java(sellerDto.getContractStart() != null ? LocalDate.parse(sellerDto.getContractStart()) : null)", target = "contractStart")
    @Mapping(expression = "java(sellerDto.getContractEnd() != null ? LocalDate.parse(sellerDto.getContractEnd()) : null)", target = "contractEnd")
    Seller toSeller(SellerDto sellerDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "status")
    @Mapping(ignore = true, target = "createdBy")
    @Mapping(ignore = true, target = "createdAt")
    @Mapping(source = "companyName", target = "companyName")
    @Mapping(source = "orgId", target = "orgId")
    @Mapping(source = "notes", target = "notes")
    @Mapping(expression = "java(sellerDto.getContractStart() != null ? LocalDate.parse(sellerDto.getContractStart()) : null)", target = "contractStart")
    @Mapping(expression = "java(sellerDto.getContractEnd() != null ? LocalDate.parse(sellerDto.getContractEnd()) : null)", target = "contractEnd")
    void updateSeller(SellerDto sellerDto, @MappingTarget Seller seller);
}
