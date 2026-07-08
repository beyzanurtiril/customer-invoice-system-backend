package com.pia.telekom.controller;

import com.pia.telekom.dto.RegionalPaymentAnalysisResponse;
import com.pia.telekom.dto.RevenueForecastResponse;
import com.pia.telekom.dto.UpgradeRecommendationResponse;
import com.pia.telekom.entity.RevenueForecast;
import com.pia.telekom.repository.RevenueForecastRepository;
import com.pia.telekom.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final RegionalPaymentAnalysisService regionalPaymentAnalysisService;
    private final UpgradeRecommendationService upgradeRecommendationService;
    private final RevenueForecastService revenueForecastService;
    private final CustomerStatsAggregationService customerStatsAggregationService;
    private final CustomerRiskAnalysisService customerRiskAnalysisService;
    private final RevenueForecastRepository revenueForecastRepository;


    @GetMapping("/regional-payments")
    public List<RegionalPaymentAnalysisResponse> getRegionalPaymentAnalysis() {
        return regionalPaymentAnalysisService.analyzeByRegion();
    }

    @GetMapping("/upgrade-recommendations")
    public List<UpgradeRecommendationResponse> getUpgradeRecommendations() {
        return upgradeRecommendationService.getRecommendations();
    }

    @GetMapping("/revenue-forecast")
    public List<RevenueForecast> getRevenueForecast() {
        return revenueForecastRepository.findAll();
    }

    @PostMapping("/recalculate-forecast")
    public List<RevenueForecast> recalculateForecast() {
        return revenueForecastService.recalculateForecast();
    }

    @PostMapping("/recalculate-stats")
    public String recalculateStats() {
        int count = customerStatsAggregationService.recalculateAll();
        return count + " müşteri için customer_stats güncellendi";
    }

    @PostMapping("/recalculate-risk")
    public String recalculateRisk() {
        int count = customerRiskAnalysisService.recalculateAll();
        return count + " müşteri için risk analizi güncellendi";

    }
}