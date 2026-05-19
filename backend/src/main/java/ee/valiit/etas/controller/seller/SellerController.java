package ee.valiit.etas.controller.seller;

import ee.valiit.etas.controller.seller.dto.*;
import ee.valiit.etas.infrastructure.error.ApiError;
import ee.valiit.etas.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;

    @GetMapping("/seller/user/{userId}")
    @Operation(summary = "Edasimüüjate nimekiri")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public List<SellerDetailResponseDto> getSellers(@PathVariable Integer userId) {
        return sellerService.findSellers();
    }
    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Edasimüüja detailvaade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Edasimüüjat ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public SellerDetailResponseDto getSeller(@PathVariable Integer sellerId) {
        return sellerService.findSeller(sellerId);
    }
    @PostMapping("/seller/user/{userId}")
    @Operation(summary = "Lisa edasimüüja")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loodud"),
            @ApiResponse(responseCode = "400", description = "Vigased andmed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Pole õigust",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "OrgId juba olemas",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    @ResponseStatus(HttpStatus.CREATED)
    public void addSeller(@PathVariable Integer userId, @Valid @RequestBody SellerDto sellerDto) {
        sellerService.addSeller(userId, sellerDto);
    }
    @PutMapping("/seller/{sellerId}")
    @Operation(summary = "Uuenda edasimüüja andmed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Vigased andmed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Edasimüüjat ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void updateSeller(@PathVariable Integer sellerId, @Valid @RequestBody SellerDto sellerDto) {
        sellerService.updateSeller(sellerId, sellerDto);
    }
    @PutMapping("/seller/{sellerId}/status")
    @Operation(summary = "Muuda edasimüüja staatus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Vigased andmed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Edasimüüjat ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Staatus juba selline",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void updateSellerStatus(@PathVariable Integer sellerId, @Valid @RequestBody SellerStatusDto sellerStatusDto) {
        sellerService.updateSellerStatus(sellerId, sellerStatusDto);
    }
}
