package ee.valiit.etas.controller.seller;

import ee.valiit.etas.controller.seller.dto.SellerContactResponseDto;
import ee.valiit.etas.infrastructure.error.ApiError;
import ee.valiit.etas.service.SellerContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SellerContactController {
    private final SellerContactService sellerContactService;

    @GetMapping("/seller/{sellerId}/contacts")
    @Operation(summary = "Edasimüüja kontaktid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Edasimüüjat ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public List<SellerContactResponseDto> getSellerContacts(@PathVariable Integer sellerId) {
        return sellerContactService.findSellerContacts(sellerId);
    }

    @DeleteMapping("/seller/{sellerId}/contacts/{contactId}")
    @Operation(summary = "Kustuta kontakt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Pole õigust",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Edasimüüjat või kontakti ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Teenustasu on kasutuses",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void deleteSellerContact(@RequestParam Integer userId,
                                    @PathVariable Integer sellerId,
                                    @PathVariable Integer contactId) {
        sellerContactService.deleteSellerContact(userId, sellerId, contactId);
    }
}
