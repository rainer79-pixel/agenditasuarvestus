package ee.valiit.etas.persistence.view;

import ee.valiit.etas.controller.report.dto.ReportDetailResponseDto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-23T15:20:17+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (JetBrains s.r.o.)"
)
@Component
public class CommissionCalculationViewMapperImpl implements CommissionCalculationViewMapper {

    @Override
    public CommissionCalculationViewDto toDto(CommissionCalculationView commissionCalculationView) {
        if ( commissionCalculationView == null ) {
            return null;
        }

        Long id = null;
        Integer salesReportId = null;
        Integer sellerId = null;
        Integer productTypeId = null;
        String companyName = null;
        String productTypeName = null;
        Long transactionCountSum = null;
        BigDecimal salesAmountSum = null;
        BigDecimal feeSum = null;
        BigDecimal feePerTransaction = null;
        BigDecimal feePercent = null;
        Boolean includesVat = null;
        BigDecimal calculatedFee = null;
        BigDecimal vatRate = null;
        BigDecimal vatAmount = null;
        BigDecimal calculatedFeePlusVat = null;

        id = commissionCalculationView.getId();
        salesReportId = commissionCalculationView.getSalesReportId();
        sellerId = commissionCalculationView.getSellerId();
        productTypeId = commissionCalculationView.getProductTypeId();
        companyName = commissionCalculationView.getCompanyName();
        productTypeName = commissionCalculationView.getProductTypeName();
        transactionCountSum = commissionCalculationView.getTransactionCountSum();
        salesAmountSum = commissionCalculationView.getSalesAmountSum();
        feeSum = commissionCalculationView.getFeeSum();
        feePerTransaction = commissionCalculationView.getFeePerTransaction();
        feePercent = commissionCalculationView.getFeePercent();
        includesVat = commissionCalculationView.getIncludesVat();
        calculatedFee = commissionCalculationView.getCalculatedFee();
        vatRate = commissionCalculationView.getVatRate();
        vatAmount = commissionCalculationView.getVatAmount();
        calculatedFeePlusVat = commissionCalculationView.getCalculatedFeePlusVat();

        CommissionCalculationViewDto commissionCalculationViewDto = new CommissionCalculationViewDto( id, salesReportId, sellerId, productTypeId, companyName, productTypeName, transactionCountSum, salesAmountSum, feeSum, feePerTransaction, feePercent, includesVat, calculatedFee, vatRate, vatAmount, calculatedFeePlusVat );

        return commissionCalculationViewDto;
    }

    @Override
    public List<CommissionCalculationViewDto> toCommissionCalculationViewDtos(List<CommissionCalculationView> commissionCalculationViews) {
        if ( commissionCalculationViews == null ) {
            return null;
        }

        List<CommissionCalculationViewDto> list = new ArrayList<CommissionCalculationViewDto>( commissionCalculationViews.size() );
        for ( CommissionCalculationView commissionCalculationView : commissionCalculationViews ) {
            list.add( toDto( commissionCalculationView ) );
        }

        return list;
    }

    @Override
    public ReportDetailResponseDto toReportDetailResponseDto(CommissionCalculationView view) {
        if ( view == null ) {
            return null;
        }

        ReportDetailResponseDto reportDetailResponseDto = new ReportDetailResponseDto();

        reportDetailResponseDto.setProductTypeName( view.getProductTypeName() );
        reportDetailResponseDto.setTransactionCount( view.getTransactionCountSum() );
        reportDetailResponseDto.setSalesAmount( view.getSalesAmountSum() );
        reportDetailResponseDto.setFeePerTransaction( view.getFeePerTransaction() );
        reportDetailResponseDto.setFeePercent( view.getFeePercent() );
        reportDetailResponseDto.setCalculatedFee( view.getCalculatedFee() );
        reportDetailResponseDto.setVatAmount( view.getVatAmount() );
        reportDetailResponseDto.setTotalFee( view.getCalculatedFeePlusVat() );

        return reportDetailResponseDto;
    }

    @Override
    public List<ReportDetailResponseDto> toReportDetailResponseDtos(List<CommissionCalculationView> views) {
        if ( views == null ) {
            return null;
        }

        List<ReportDetailResponseDto> list = new ArrayList<ReportDetailResponseDto>( views.size() );
        for ( CommissionCalculationView commissionCalculationView : views ) {
            list.add( toReportDetailResponseDto( commissionCalculationView ) );
        }

        return list;
    }
}
