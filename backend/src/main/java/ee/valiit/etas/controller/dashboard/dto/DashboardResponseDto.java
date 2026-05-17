package ee.valiit.etas.controller.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class DashboardResponseDto {
    private Integer sellerCount;
    private String lastImport;
}
