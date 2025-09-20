package com.example.paymentsimulator.services;

import java.math.BigDecimal;

public class CommissionService {

    private final CommissionRuleProvider provider;

    public CommissionService(CommissionRuleProvider provider) {
        this.provider = provider;
    }

    public BigDecimal commissionOf(BigDecimal baseAmount) {
        if (baseAmount == null) return BigDecimal.ZERO;
        BigDecimal pct = provider.percentFor(baseAmount);
        return baseAmount.multiply(pct).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}