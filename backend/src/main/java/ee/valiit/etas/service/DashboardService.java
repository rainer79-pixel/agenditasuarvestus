package ee.valiit.etas.service;

import ee.valiit.etas.controller.dashboard.dto.DashboardResponseDto;
import ee.valiit.etas.persistence.salesreport.SalesReportRepository;
import ee.valiit.etas.persistence.seller.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static ee.valiit.etas.Status.ACTIVE;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final SellerRepository sellerRepository;
    private final SalesReportRepository salesReportRepository;

    public DashboardResponseDto getDashboardResponse() {
        int sellerCount = sellerRepository.countSellersBy(ACTIVE.getCode());
        Optional<LocalDate> lastImportDate = salesReportRepository.findLastImportDate();
        String lastImport = lastImportDate
                .map(date -> date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .orElse(null);
        return new DashboardResponseDto(sellerCount, lastImport);
    }
}
