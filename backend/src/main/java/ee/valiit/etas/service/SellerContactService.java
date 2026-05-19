package ee.valiit.etas.service;

import ee.valiit.etas.controller.seller.dto.SellerContactResponseDto;
import ee.valiit.etas.infrastructure.exception.DataNotFoundException;
import ee.valiit.etas.infrastructure.exception.ForbiddenException;
import ee.valiit.etas.persistence.seller.SellerRepository;
import ee.valiit.etas.persistence.sellercontact.SellerContact;
import ee.valiit.etas.persistence.sellercontact.SellerContactMapper;
import ee.valiit.etas.persistence.sellercontact.SellerContactRepository;
import ee.valiit.etas.persistence.sellercontactrole.SellerContactRoleRepository;
import ee.valiit.etas.persistence.user.User;
import ee.valiit.etas.persistence.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static ee.valiit.etas.infrastructure.error.ErrorResponse.*;

@Service
@RequiredArgsConstructor
public class SellerContactService {
    private final SellerContactRepository sellerContactRepository;
    private final SellerContactRoleRepository sellerContactRoleRepository;
    private final SellerContactMapper sellerContactMapper;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;

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
        validateUserIsAdmin(userId);
        validateSellerExists(sellerId);
        validateContactExists(contactId);
        deleteContactWithRoles(contactId);
    }

    private void validateUserIsAdmin(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND.getMessage(), USER_NOT_FOUND.getErrorCode()));
        if (!"A".equals(user.getUserRole())) {
            throw new ForbiddenException(ACCESS_DENIED.getMessage(), ACCESS_DENIED.getErrorCode());
        }
    }

    private void validateSellerExists(Integer sellerId) {
        if (!sellerRepository.existsById(sellerId)) {
            throw new DataNotFoundException(SELLER_NOT_FOUND.getMessage(), SELLER_NOT_FOUND.getErrorCode());
        }
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
}
