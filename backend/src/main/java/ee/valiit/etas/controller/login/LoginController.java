package ee.valiit.etas.controller.login;

import ee.valiit.etas.controller.login.dto.LoginDto;
import ee.valiit.etas.controller.login.dto.LoginResponseDto;
import ee.valiit.etas.infrastructure.error.ApiError;
import ee.valiit.etas.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    @Operation(summary = "Sisselogimine",
            description = "Otsitakse kasutajat email + parool + aktiivne staatus järgi. " +
                    "Kui vastet ei leita, tagastatakse 403. " +
                    "Andmed edastatakse POST body-s, mitte URL-is.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403",
                    description = "Sisselogimine ebaõnnestus, võtke ühendust administraatoriga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public LoginResponseDto login(@Valid @RequestBody LoginDto loginDto) {
        return loginService.getLoginResponse(loginDto.getEmail(), loginDto.getPassword());
    }
}
