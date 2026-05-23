package ee.valiit.etas.service;

import ee.valiit.etas.controller.seller.dto.SellerContactDto;
import ee.valiit.etas.controller.seller.dto.SellerContactResponseDto;
import ee.valiit.etas.infrastructure.exception.DataNotFoundException;
import ee.valiit.etas.persistence.contactrole.ContactRole;
import ee.valiit.etas.persistence.contactrole.ContactRoleRepository;
import ee.valiit.etas.persistence.seller.Seller;
import ee.valiit.etas.persistence.seller.SellerRepository;
import ee.valiit.etas.persistence.sellercontact.SellerContact;
import ee.valiit.etas.persistence.sellercontact.SellerContactMapper;
import ee.valiit.etas.persistence.sellercontact.SellerContactRepository;
import ee.valiit.etas.persistence.sellercontactrole.SellerContactRole;
import ee.valiit.etas.persistence.sellercontactrole.SellerContactRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static ee.valiit.etas.infrastructure.error.ErrorResponse.*;

@Service
@RequiredArgsConstructor
public class SellerContactService {
    private final SellerContactRepository sellerContactRepository;
    private final SellerContactRoleRepository sellerContactRoleRepository;
    private final SellerContactMapper sellerContactMapper;
    private final SellerRepository sellerRepository;
    private final ContactRoleRepository contactRoleRepository;
    private final ValidationService validationService;

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

    @Transactional
    public void deleteSellerContact(Integer userId, Integer sellerId, Integer contactId) {
        validationService.validateUserIsAdmin(userId);
        validationService.validateSellerExists(sellerId);
        validateContactExists(contactId);
        deleteContactWithRoles(contactId);
    }

    @Transactional
    public void addSellerContact(Integer userId, Integer sellerId, SellerContactDto sellerContactDto) {
        validationService.validateUserIsAdmin(userId);
        validationService.validateSellerExists(sellerId);
        createAndSaveSellerContact(sellerId, sellerContactDto);
    }

    private void validateContactExists(Integer contactId) {
        if (!sellerContactRepository.existsById(contactId)) {
            throw new DataNotFoundException(CONTACT_NOT_FOUND.getMessage(), CONTACT_NOT_FOUND.getErrorCode());
        }
    }

    private void deleteContactWithRoles(Integer contactId) {
        sellerContactRoleRepository.deleteAllBySellerContactId(contactId);
        sellerContactRepository.deleteById(contactId);
    }

    private void createAndSaveSellerContact(Integer sellerId, SellerContactDto sellerContactDto) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new DataNotFoundException(SELLER_NOT_FOUND.getMessage(), SELLER_NOT_FOUND.getErrorCode()));
        SellerContact contact = sellerContactMapper.toSellerContact(sellerContactDto);
        contact.setSeller(seller);
        SellerContact saved = sellerContactRepository.save(contact);
        for (String sellerContactRoleId : sellerContactDto.getRoles()) {
            ContactRole contactRole = contactRoleRepository.findByCode(sellerContactRoleId)
                    .orElseThrow(() -> new DataNotFoundException(ROLE_NOT_FOUND.getMessage(), ROLE_NOT_FOUND.getErrorCode()));
            SellerContactRole sellerContactRole = new SellerContactRole();
            sellerContactRole.setSellerContact(saved);
            sellerContactRole.setContactRole(contactRole);
            sellerContactRoleRepository.save(sellerContactRole);
        }
    }
}
