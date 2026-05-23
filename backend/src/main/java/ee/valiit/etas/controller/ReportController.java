package ee.valiit.etas.controller;

import ee.valiit.etas.controller.report.dto.ReportDetailResponseDto;
import ee.valiit.etas.controller.report.dto.ReportResponseDto;
import ee.valiit.etas.infrastructure.error.ApiError;
import ee.valiit.etas.service.ReportControllerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {
    private final ReportControllerService reportControllerService;

    @GetMapping("/report/user/{userId}")
    @Operation(summary = "Tagasta kasutaja aruannete nimekiri filtritega")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aruanded leitud"),
            @ApiResponse(responseCode = "404", description = "Aruandeid ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<ReportResponseDto> getReports(
            @PathVariable Integer userId,
            @RequestParam(required = false) String periodFrom,
            @RequestParam(required = false) String periodTo,
            @RequestParam(required = false) Integer sellerId) {
        return reportControllerService.getReports(userId, periodFrom, periodTo, sellerId);
    }

    @PostMapping("/import/user/{userId}")
    public void addReport(
            @PathVariable Integer userId,
            @RequestParam("file") MultipartFile file) {
        reportControllerService.addReport(userId, file);
    }
    @GetMapping("/report/{sellerId}/{period}")
    @Operation(summary = "Edasimüüja aruande tootegrupp-põhised detailid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Andmed laaditud"),
            @ApiResponse(responseCode = "404", description = "Aruannet ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Serveri viga",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public List<ReportDetailResponseDto> getReport(
            @PathVariable Integer sellerId,
            @PathVariable String period){
        return reportControllerService.getSellersReportDetails(sellerId, period);
    }

}
