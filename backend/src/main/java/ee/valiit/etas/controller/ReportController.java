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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(required = false) String periodFrom,
            @RequestParam(required = false) String periodTo,
            @RequestParam(required = false) Integer sellerId) {
        return reportControllerService.getReports(periodFrom, periodTo, sellerId);
    }

    @PostMapping("/import/user/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Laadi üles Excel müügiaruanne")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Aruanne imporditud"),
            @ApiResponse(responseCode = "400", description = "Vigased päised",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Kasutajal pole õigust",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Sellel perioodil on aruanne juba olemas",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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
    @GetMapping("/report/user/{userId}/export")
    @Operation(summary = "Ekspordi aruanne Excelisse")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fail genereeritud"),
            @ApiResponse(responseCode = "404", description = "Aruandeid ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<byte[]> exportReports(
            @RequestParam(required = false) String periodFrom,
            @RequestParam(required = false) String periodTo,
            @RequestParam(required = false) Integer sellerId) {
        byte[] file = reportControllerService.exportReports(periodFrom, periodTo, sellerId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "aruanne.xlsx");
        return new ResponseEntity<>(file, headers, HttpStatus.OK);
    }

    @DeleteMapping("/import/user/{userId}/{period}")
    @Operation(summary = "Kustuta imporditud perioodi andmed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Periood kustutatud"),
            @ApiResponse(responseCode = "403", description = "Kasutajal pole õigust",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Perioodi andmeid ei leitud",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void deleteReport(
            @PathVariable Integer userId,
            @PathVariable String period) {
        reportControllerService.deleteReport(userId, period);
    }
}
