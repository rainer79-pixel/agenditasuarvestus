package ee.valiit.etas.service;

import ee.valiit.etas.controller.login.dto.LoginResponseDto;
import ee.valiit.etas.infrastructure.exception.ForbiddenException;
import ee.valiit.etas.persistence.user.User;
import ee.valiit.etas.persistence.user.UserMapper;
import ee.valiit.etas.persistence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ee.valiit.etas.Status.ACTIVE;
import static ee.valiit.etas.infrastructure.error.ErrorResponse.INCORRECT_CREDENTIALS;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public LoginResponseDto getLoginResponse(String email, String password) {
        User user = findUserBy(email, password);
        return userMapper.toLoginResponseDto(user);
    }

    private User findUserBy(String email, String password) {
        return userRepository.findUserBy(email, password, ACTIVE.getCode())
                .orElseThrow(() -> new ForbiddenException(INCORRECT_CREDENTIALS.getMessage(), INCORRECT_CREDENTIALS.getErrorCode()));
    }
}

