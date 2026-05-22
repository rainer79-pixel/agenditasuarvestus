package ee.valiit.etas.service;

import ee.valiit.etas.controller.report.dto.SalesReportRowDto;
import ee.valiit.etas.infrastructure.exception.DataNotFoundException;
import ee.valiit.etas.infrastructure.exception.ForbiddenException;
import ee.valiit.etas.persistence.CommissionCalculationViewRepository;
import ee.valiit.etas.persistence.commissioncalculation.CommissionCalculation;
import ee.valiit.etas.persistence.commissioncalculation.CommissionCalculationRepository;
import ee.valiit.etas.persistence.commissionrate.CommissionRate;
import ee.valiit.etas.persistence.commissionrate.CommissionRateRepository;
import ee.valiit.etas.persistence.salesreport.SalesReport;
import ee.valiit.etas.persistence.salesreport.SalesReportRepository;
import ee.valiit.etas.persistence.salesreportdetail.SalesReportDetail;
import ee.valiit.etas.persistence.salesreportdetail.SalesReportDetailMapper;
import ee.valiit.etas.persistence.salesreportdetail.SalesReportDetailRepository;
import ee.valiit.etas.persistence.seller.Seller;
import ee.valiit.etas.persistence.seller.SellerRepository;
import ee.valiit.etas.persistence.user.User;
import ee.valiit.etas.persistence.user.UserRepository;
import ee.valiit.etas.persistence.view.CommissionCalculationView;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ee.valiit.etas.infrastructure.error.ErrorResponse.*;

@Service
@RequiredArgsConstructor
public class ReportControllerService {

    private static final List<String> REQUIRED_HEADERS = List.of(
            "issuer_name", "seller_name", "seller_org_id", "dept_name", "seller_dept_id",
            "payment_channel", "tyyp", "tehinguid", "summas", "price_add_sum",
            "fee_sum", "a_date", "buyer_channel", "piirkond", "liiniomanik"
    );

    private final SalesReportRepository salesReportRepository;
    private final SalesReportDetailRepository salesReportDetailRepository;
    private final SalesReportDetailMapper salesReportDetailMapper;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final CommissionCalculationViewRepository commissionCalculationViewRepository;
    private final CommissionCalculationRepository commissionCalculationRepository;
    private final CommissionRateRepository commissionRateRepository;

    @Transactional
    public void addReport(Integer userId, MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headerMap = buildHeaderMap(sheet.getRow(0));
            validateHeaders(headerMap);
            List<SalesReportRowDto> rows = parseRows(sheet, headerMap);
            String period = rows.get(0).getPeriod();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND.getMessage(), USER_NOT_FOUND.getErrorCode()));
            SalesReport salesReport = createAndSaveSalesReport(user, period);
            List<SalesReportDetail> details = salesReportDetailMapper.toSalesReportDetails(rows);
            List<SalesReportDetail> validDetails = attachSalesReportAndSeller(details, rows, salesReport);
            salesReportDetailRepository.saveAll(validDetails);
            createCommissionCalculations(salesReport);
        } catch (IOException e) {
            throw new RuntimeException("Faili lugemine ebaõnnestus");
        }

    }

    private Map<String, Integer> buildHeaderMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            map.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
        }
        return map;
    }

    private void validateHeaders(Map<String, Integer> headerMap) {
        if (!headerMap.keySet().containsAll(REQUIRED_HEADERS)) {
            throw new ForbiddenException(IMPORT_INVALID_HEADER.getMessage(), IMPORT_INVALID_HEADER.getErrorCode());
        }
    }

    private List<SalesReportRowDto> parseRows(Sheet sheet, Map<String, Integer> headerMap) {
        List<SalesReportRowDto> rows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || row.getCell(0) == null) break;
            rows.add(toSalesReportRowDto(row, headerMap));
        }
        return rows;
    }

    private SalesReportRowDto toSalesReportRowDto(Row row, Map<String, Integer> headerMap) {
        SalesReportRowDto dto = new SalesReportRowDto();
        dto.setIssuerName(getStringCell(row, headerMap.get("issuer_name")));
        dto.setSellerName(getStringCell(row, headerMap.get("seller_name")));
        dto.setOrgId(getIntCell(row, headerMap.get("seller_org_id")));
        dto.setDepartmentName(getStringCell(row, headerMap.get("dept_name")));
        dto.setDepartmentId(getStringCell(row, headerMap.get("seller_dept_id")));
        dto.setPaymentChannel(getStringCell(row, headerMap.get("payment_channel")));
        dto.setProductType(getStringCell(row, headerMap.get("tyyp")));
        dto.setTransactionCount(getIntCell(row, headerMap.get("tehinguid")));
        dto.setSalesAmount(getBigDecimalCell(row, headerMap.get("summas")));
        dto.setPriceAddSum(getBigDecimalCell(row, headerMap.get("price_add_sum")));
        dto.setFeeSum(getBigDecimalCell(row, headerMap.get("fee_sum")));
        dto.setPeriod(getStringCell(row, headerMap.get("a_date")));
        dto.setBuyerChannel(getStringCell(row, headerMap.get("buyer_channel")));
        dto.setRegion(getStringCell(row, headerMap.get("piirkond")));
        dto.setLiiniomanik(getStringCell(row, headerMap.get("liiniomanik")));
        return dto;
    }

    private SalesReport createAndSaveSalesReport(User user, String period) {
        SalesReport salesReport = new SalesReport();
        salesReport.setCreatedBy(user);
        salesReport.setPeriod(period);
        salesReport.setCreatedAt(LocalDate.now());
        return salesReportRepository.save(salesReport);
    }

    private void createCommissionCalculations(SalesReport salesReport) {
        List<CommissionCalculationView> viewRows = commissionCalculationViewRepository.findBySalesReportId(salesReport.getId());
        List<CommissionCalculation> calculations = new ArrayList<>();
        for (CommissionCalculationView row : viewRows) {
            CommissionRate commissionRate = commissionRateRepository.findById(row.getCommissionRateId())
                    .orElseThrow(() -> new DataNotFoundException(COMMISSION_RATE_NOT_FOUND.getMessage(), COMMISSION_RATE_NOT_FOUND.getErrorCode()));
            CommissionCalculation calculation = new CommissionCalculation();
            calculation.setSalesReport(salesReport);
            calculation.setCommissionRate(commissionRate);
            calculation.setCalculatedFee(row.getCalculatedFee());
            calculation.setVatAmount(row.getVatAmount());
            calculation.setCalculatedFeePlusVat(row.getCalculatedFeePlusVat());
            calculation.setCalculationDate(LocalDate.now());
            calculations.add(calculation);
        }
        commissionCalculationRepository.saveAll(calculations);
    }

    private List<SalesReportDetail> attachSalesReportAndSeller(List<SalesReportDetail> details, List<SalesReportRowDto> rows, SalesReport salesReport) {
        List<SalesReportDetail> validDetails = new ArrayList<>();
        for (int i = 0; i < details.size(); i++) {
            SalesReportDetail detail = details.get(i);
            Integer orgId = rows.get(i).getOrgId();
            Optional<Seller> seller = sellerRepository.findByOrgId(orgId);
            if (seller.isEmpty()) continue;
            detail.setSalesReport(salesReport);
            detail.setSeller(seller.get());
            detail.setCreatedAt(Instant.now());
            validDetails.add(detail);
        }
        return validDetails;
    }

    private String getStringCell(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    private Integer getIntCell(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> Integer.parseInt(cell.getStringCellValue().trim());
            default -> null;
        };
    }

    private BigDecimal getBigDecimalCell(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> new BigDecimal(cell.getStringCellValue().trim());
            default -> null;
        };
    }
}
