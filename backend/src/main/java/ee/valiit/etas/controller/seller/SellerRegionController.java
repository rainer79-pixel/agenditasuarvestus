package ee.valiit.etas.controller.seller;

import ee.valiit.etas.controller.seller.dto.SellerRegionResponseDto;
import ee.valiit.etas.infrastructure.error.ApiError;
import ee.valiit.etas.service.SellerRegionService;
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
public class SellerRegionController {
    private final SellerRegionService sellerRegionService;

    @GetMapping("/seller/{sellerId}/regions")
    @Operation(summary = "Edasimüüja piirkonnad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Edasimüüjat ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public List<SellerRegionResponseDto> getSellerRegions(@PathVariable Integer sellerId) {
        return sellerRegionService.findSellerRegions(sellerId);
    }

    @DeleteMapping("/seller/{sellerId}/regions/{regionId}")
    @Operation(summary = "Kustuta edasimüüja piirkond")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Pole õigust",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Edasimüüjat või piirkonda ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void deleteSellerRegion(@RequestParam Integer userId,
                                   @PathVariable Integer sellerId,
                                   @PathVariable Integer regionId) {
        sellerRegionService.deleteSellerRegion(userId, sellerId, regionId);
    }
}
