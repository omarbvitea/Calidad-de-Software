package com.example.paymentsimulator.services;

import java.math.BigDecimal;

/**
 * Calcula la tasa de comisión por tramos.
 * Regla asumida: montos < 1000 => 8%, montos >= 1000 => 5%.
 * [min,1000] y [1000,max] para no solapar
 * tomamos 1000 como límite inferior del segundo tramo.
 */
public class CommissionService {

    public BigDecimal commissionRate(BigDecimal baseAmount) {
        if (baseAmount == null) return BigDecimal.ZERO;
        return baseAmount.compareTo(new BigDecimal("1000")) < 0
                ? new BigDecimal("0.08")
                : new BigDecimal("0.05");
    }

    public BigDecimal commissionOf(BigDecimal baseAmount) {
        return baseAmount.multiply(commissionRate(baseAmount));
    }
}
