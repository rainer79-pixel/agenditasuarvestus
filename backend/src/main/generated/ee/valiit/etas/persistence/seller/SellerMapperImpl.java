package ee.valiit.etas.persistence.seller;

import ee.valiit.etas.Status;
import ee.valiit.etas.controller.seller.dto.SellerDetailResponseDto;
import ee.valiit.etas.controller.seller.dto.SellerDto;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-19T09:12:39+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (JetBrains s.r.o.)"
)
@Component
public class SellerMapperImpl implements SellerMapper {

    @Override
    public SellerDetailResponseDto toSellerDetailResponseDto(Seller seller) {
        if ( seller == null ) {
            return null;
        }

        SellerDetailResponseDto sellerDetailResponseDto = new SellerDetailResponseDto();

        sellerDetailResponseDto.setSellerId( seller.getId() );
        sellerDetailResponseDto.setCompanyName( seller.getCompanyName() );
        sellerDetailResponseDto.setOrgId( seller.getOrgId() );
        sellerDetailResponseDto.setNotes( seller.getNotes() );

        sellerDetailResponseDto.setStatus( Status.toApiValue(seller.getStatus()) );
        sellerDetailResponseDto.setContractStart( seller.getContractStart() != null ? seller.getContractStart().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : null );
        sellerDetailResponseDto.setContractEnd( seller.getContractEnd() != null ? seller.getContractEnd().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : null );

        return sellerDetailResponseDto;
    }

    @Override
    public List<SellerDetailResponseDto> toSellerDetailResponseDtos(List<Seller> sellers) {
        if ( sellers == null ) {
            return null;
        }

        List<SellerDetailResponseDto> list = new ArrayList<SellerDetailResponseDto>( sellers.size() );
        for ( Seller seller : sellers ) {
            list.add( toSellerDetailResponseDto( seller ) );
        }

        return list;
    }

    @Override
    public Seller toSeller(SellerDto sellerDto) {
        if ( sellerDto == null ) {
            return null;
        }

        Seller seller = new Seller();

        seller.setCompanyName( sellerDto.getCompanyName() );
        seller.setOrgId( sellerDto.getOrgId() );
        seller.setNotes( sellerDto.getNotes() );

        seller.setStatus( Status.ACTIVE.getCode() );
        seller.setContractStart( sellerDto.getContractStart() != null ? LocalDate.parse(sellerDto.getContractStart()) : null );
        seller.setContractEnd( sellerDto.getContractEnd() != null ? LocalDate.parse(sellerDto.getContractEnd()) : null );

        return seller;
    }

    @Override
    public void updateSeller(SellerDto sellerDto, Seller seller) {
        if ( sellerDto == null ) {
            return;
        }

        if ( sellerDto.getCompanyName() != null ) {
            seller.setCompanyName( sellerDto.getCompanyName() );
        }
        if ( sellerDto.getOrgId() != null ) {
            seller.setOrgId( sellerDto.getOrgId() );
        }
        if ( sellerDto.getNotes() != null ) {
            seller.setNotes( sellerDto.getNotes() );
        }

        seller.setContractStart( sellerDto.getContractStart() != null ? LocalDate.parse(sellerDto.getContractStart()) : null );
        seller.setContractEnd( sellerDto.getContractEnd() != null ? LocalDate.parse(sellerDto.getContractEnd()) : null );
    }
}
