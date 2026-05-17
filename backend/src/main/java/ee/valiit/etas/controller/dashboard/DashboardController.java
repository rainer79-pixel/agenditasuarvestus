package ee.valiit.etas.controller.dashboard;

import ee.valiit.etas.controller.dashboard.dto.DashboardResponseDto;
import ee.valiit.etas.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard statistika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")})
    public DashboardResponseDto getDashboard() {
        return dashboardService.getDashboardResponse();
    }
}
