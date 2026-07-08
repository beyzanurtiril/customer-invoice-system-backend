package com.pia.telekom.dto;

import java.math.BigDecimal;

public record CustomerRiskAnalysisResponse(
        Integer customerId,
        BigDecimal riskScore,
        String behaviorCategory,
        String recommendAction
) {
}