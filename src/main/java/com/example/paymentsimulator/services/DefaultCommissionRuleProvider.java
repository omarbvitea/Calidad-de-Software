package com.example.paymentsimulator.services;

import java.math.BigDecimal;

public class DefaultCommissionRuleProvider implements CommissionRuleProvider {
  @Override
  public BigDecimal percentFor(BigDecimal baseAmount) {
    if (baseAmount == null) return BigDecimal.ZERO;
    return baseAmount.compareTo(new BigDecimal("1000")) < 0
        ? new BigDecimal("0.08")
        : new BigDecimal("0.05");
  }
}