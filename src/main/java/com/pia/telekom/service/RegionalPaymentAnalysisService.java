package com.pia.telekom.service;

import com.pia.telekom.dto.RegionalPaymentAnalysisResponse;
import com.pia.telekom.entity.Invoice;
import com.pia.telekom.entity.Region;
import com.pia.telekom.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionalPaymentAnalysisService {

    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public List<RegionalPaymentAnalysisResponse> analyzeByRegion() {
        List<Invoice> allInvoices = invoiceRepository.findAllWithCustomerAndRegion(); // değişen satır

        Map<Region, List<Invoice>> groupedByRegion = allInvoices.stream()
                .collect(Collectors.groupingBy(invoice -> invoice.getCustomer().getRegion()));

        return groupedByRegion.entrySet().stream()
                .map(entry -> buildAnalysis(entry.getKey(), entry.getValue()))
                .toList();
    }

    private RegionalPaymentAnalysisResponse buildAnalysis(Region region, List<Invoice> invoices) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long overdueCount = 0;

        for (Invoice invoice : invoices) {
            double amount = invoice.getInvoiceAmount().doubleValue();
            stats.addValue(amount);
            totalRevenue = totalRevenue.add(invoice.getInvoiceAmount());

            if (invoice.getPaymentDate() == null
                    && invoice.getDueDate() != null
                    && invoice.getDueDate().isBefore(LocalDate.now())) {
                overdueCount++;
            }
        }

        double overdueRate = invoices.isEmpty() ? 0.0 : (overdueCount * 100.0) / invoices.size();

        return new RegionalPaymentAnalysisResponse(
                region.getRegionId(),
                region.getName(),
                region.getCityType(),
                invoices.size(),
                totalRevenue,
                round(stats.getMean()),
                round(stats.getStandardDeviation()),
                round(overdueRate)
        );
    }

    private double round(double value) {
        if (Double.isNaN(value)) {
            return 0.0;
        }
        return Math.round(value * 100.0) / 100.0;
    }
}