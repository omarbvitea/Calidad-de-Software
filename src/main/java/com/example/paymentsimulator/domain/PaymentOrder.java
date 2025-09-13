package com.example.paymentsimulator.domain;

import java.time.Instant;
import java.util.List;

/**
 * Orden de pago generada como resultado del caso de uso.
 */
public record PaymentOrder(
        String orderCode,
        Money originalAmount,
        Money discountTotal,
        Money commission,
        Money finalAmount,
        Card card,
        List<String> appliedCoupons,
        Instant createdAt
) {}
