package com.example.paymentsimulator.services;

import java.math.BigDecimal;

public interface CommissionRuleProvider {
    BigDecimal percentFor(BigDecimal baseAmount);
}
