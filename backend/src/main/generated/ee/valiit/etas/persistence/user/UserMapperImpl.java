package ee.valiit.etas.persistence.user;

import ee.valiit.etas.controller.login.dto.LoginResponseDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-22T15:11:46+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (JetBrains s.r.o.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public LoginResponseDto toLoginResponseDto(User user) {
        if ( user == null ) {
            return null;
        }

        LoginResponseDto loginResponseDto = new LoginResponseDto();

        loginResponseDto.setUserId( user.getId() );
        loginResponseDto.setRole( user.getUserRole() );
        loginResponseDto.setFirstName( user.getFirstName() );
        loginResponseDto.setMiddleName( user.getMiddleName() );
        loginResponseDto.setLastName( user.getLastName() );

        return loginResponseDto;
    }
}
