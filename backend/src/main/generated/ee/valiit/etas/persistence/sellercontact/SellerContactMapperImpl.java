package ee.valiit.etas.persistence.sellercontact;

import ee.valiit.etas.controller.seller.dto.SellerContactDto;
import ee.valiit.etas.controller.seller.dto.SellerContactResponseDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-21T14:43:31+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (JetBrains s.r.o.)"
)
@Component
public class SellerContactMapperImpl implements SellerContactMapper {

    @Override
    public SellerContactResponseDto toSellerContactResponseDto(SellerContact sellerContact) {
        if ( sellerContact == null ) {
            return null;
        }

        SellerContactResponseDto sellerContactResponseDto = new SellerContactResponseDto();

        sellerContactResponseDto.setContactId( sellerContact.getId() );
        sellerContactResponseDto.setFirstName( sellerContact.getFirstName() );
        sellerContactResponseDto.setMiddleName( sellerContact.getMiddleName() );
        sellerContactResponseDto.setLastName( sellerContact.getLastName() );
        sellerContactResponseDto.setPhone( sellerContact.getPhone() );
        sellerContactResponseDto.setEmail( sellerContact.getEmail() );

        return sellerContactResponseDto;
    }

    @Override
    public SellerContact toSellerContact(SellerContactDto sellerContactDto) {
        if ( sellerContactDto == null ) {
            return null;
        }

        SellerContact sellerContact = new SellerContact();

        sellerContact.setFirstName( sellerContactDto.getFirstName() );
        sellerContact.setMiddleName( sellerContactDto.getMiddleName() );
        sellerContact.setLastName( sellerContactDto.getLastName() );
        sellerContact.setPhone( sellerContactDto.getPhone() );
        sellerContact.setEmail( sellerContactDto.getEmail() );

        return sellerContact;
    }
}
