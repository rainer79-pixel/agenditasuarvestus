package ee.valiit.etas.persistence.commissionrate;

import ee.valiit.etas.controller.seller.dto.CommissionRateDto;
import ee.valiit.etas.controller.seller.dto.CommissionRateResponseDto;
import ee.valiit.etas.persistence.producttype.ProductType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-22T11:42:19+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (JetBrains s.r.o.)"
)
@Component
public class CommissionRateMapperImpl implements CommissionRateMapper {

    @Override
    public CommissionRateResponseDto toCommissionRateResponseDto(CommissionRate commissionRate) {
        if ( commissionRate == null ) {
            return null;
        }

        CommissionRateResponseDto commissionRateResponseDto = new CommissionRateResponseDto();

        commissionRateResponseDto.setCommissionRateId( commissionRate.getId() );
        commissionRateResponseDto.setProductTypeName( commissionRateProductTypeProductTypeName( commissionRate ) );
        commissionRateResponseDto.setFeePerTransaction( commissionRate.getFeePerTransaction() );
        commissionRateResponseDto.setFeePercent( commissionRate.getFeePercent() );
        commissionRateResponseDto.setIncludesVat( commissionRate.getIncludesVat() );

        commissionRateResponseDto.setValidFrom( commissionRate.getValidFrom() != null ? commissionRate.getValidFrom().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : null );
        commissionRateResponseDto.setValidTo( commissionRate.getValidTo() != null ? commissionRate.getValidTo().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : null );

        return commissionRateResponseDto;
    }

    @Override
    public List<CommissionRateResponseDto> toCommissionRateResponseDtos(List<CommissionRate> commissionRates) {
        if ( commissionRates == null ) {
            return null;
        }

        List<CommissionRateResponseDto> list = new ArrayList<CommissionRateResponseDto>( commissionRates.size() );
        for ( CommissionRate commissionRate : commissionRates ) {
            list.add( toCommissionRateResponseDto( commissionRate ) );
        }

        return list;
    }

    @Override
    public void updateCommissionRate(CommissionRateDto commissionRateDto, CommissionRate commissionRate) {
        if ( commissionRateDto == null ) {
            return;
        }

        if ( commissionRateDto.getFeePerTransaction() != null ) {
            commissionRate.setFeePerTransaction( commissionRateDto.getFeePerTransaction() );
        }
        if ( commissionRateDto.getFeePercent() != null ) {
            commissionRate.setFeePercent( commissionRateDto.getFeePercent() );
        }
        if ( commissionRateDto.getIncludesVat() != null ) {
            commissionRate.setIncludesVat( commissionRateDto.getIncludesVat() );
        }

        commissionRate.setValidFrom( commissionRateDto.getValidFrom() != null ? LocalDate.parse(commissionRateDto.getValidFrom(), DateTimeFormatter.ofPattern("dd.MM.yyyy")) : null );
        commissionRate.setValidTo( commissionRateDto.getValidTo() != null ? LocalDate.parse(commissionRateDto.getValidTo(), DateTimeFormatter.ofPattern("dd.MM.yyyy")) : null );
    }

    @Override
    public CommissionRate toCommissionRate(CommissionRateDto commissionRateDto) {
        if ( commissionRateDto == null ) {
            return null;
        }

        CommissionRate commissionRate = new CommissionRate();

        commissionRate.setFeePerTransaction( commissionRateDto.getFeePerTransaction() );
        commissionRate.setFeePercent( commissionRateDto.getFeePercent() );
        commissionRate.setIncludesVat( commissionRateDto.getIncludesVat() );

        commissionRate.setValidFrom( commissionRateDto.getValidFrom() != null ? LocalDate.parse(commissionRateDto.getValidFrom(), DateTimeFormatter.ofPattern("dd.MM.yyyy")) : null );
        commissionRate.setValidTo( commissionRateDto.getValidTo() != null ? LocalDate.parse(commissionRateDto.getValidTo(), DateTimeFormatter.ofPattern("dd.MM.yyyy")) : null );

        return commissionRate;
    }

    private String commissionRateProductTypeProductTypeName(CommissionRate commissionRate) {
        ProductType productType = commissionRate.getProductType();
        if ( productType == null ) {
            return null;
        }
        return productType.getProductTypeName();
    }
}
