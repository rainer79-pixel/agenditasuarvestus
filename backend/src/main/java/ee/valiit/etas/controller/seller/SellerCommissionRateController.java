package ee.valiit.etas.controller.seller;

import ee.valiit.etas.controller.seller.dto.CommissionRateDto;
import ee.valiit.etas.controller.seller.dto.CommissionRateResponseDto;
import ee.valiit.etas.infrastructure.error.ApiError;
import ee.valiit.etas.service.SellerCommissionRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SellerCommissionRateController {
    private final SellerCommissionRateService sellerCommissionRateService;

    @GetMapping("/seller/{sellerId}/commission-rates")
    @Operation(summary = "Edasimüüja teenustasud")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Edasimüüjat ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public List<CommissionRateResponseDto> getSellerCommissionRates(@PathVariable Integer sellerId) {
        return sellerCommissionRateService.findSellerCommissionRates(sellerId);
    }

    @PutMapping("/seller/{sellerId}/commission-rates/{commissionRateId}")
    @Operation(summary = "Uuenda edasimüüja teenustasu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Kasutajal pole õigust",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Edasimüüjat ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void updateCommissionRate(@PathVariable Integer sellerId,
                                     @PathVariable Integer commissionRateId,
                                     @Valid @RequestBody CommissionRateDto commissionRateDto) {
        sellerCommissionRateService.updateCommissionRate(sellerId, commissionRateId, commissionRateDto);
    }

    @DeleteMapping("/seller/{sellerId}/commission-rates/{commissionRateId}")
    @Operation(summary = "Kustuta teenustasu määr")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Pole õigust",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Teenustasu määra ei leidu",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Teenustasu on kasutuses",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void deleteSellerCommissionRate(@RequestParam Integer userId,
                                           @PathVariable Integer sellerId,
                                           @PathVariable Integer commissionRateId) {
        sellerCommissionRateService.deleteSellerCommissionRate(userId, sellerId, commissionRateId);
    }
}