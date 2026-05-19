package ee.valiit.etas.service;

import ee.valiit.etas.controller.seller.dto.SellerContactResponseDto;
import ee.valiit.etas.persistence.sellercontact.SellerContact;
import ee.valiit.etas.persistence.sellercontact.SellerContactMapper;
import ee.valiit.etas.persistence.sellercontact.SellerContactRepository;
import ee.valiit.etas.persistence.sellercontactrole.SellerContactRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerContactService {
    private final SellerContactRepository sellerContactRepository;
    private final SellerContactRoleRepository sellerContactRoleRepository;
    private final SellerContactMapper sellerContactMapper;

    public List<SellerContactResponseDto> findSellerContacts(Integer sellerId) {
        List<SellerContact> contacts = sellerContactRepository.findSellerContactsBy(sellerId);
        List<SellerContactResponseDto> result = new ArrayList<>();
        for (SellerContact contact : contacts) {
            SellerContactResponseDto dto = sellerContactMapper.toSellerContactResponseDto(contact);
            dto.setRoles(sellerContactRoleRepository.findRoleCodesBy(contact.getId()));
            result.add(dto);
        }
        return result;
    }
}
