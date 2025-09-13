package com.example.paymentsimulator.domain;

import java.math.BigDecimal;

/**
 * Cupón con límite de usos totales (maxUsesTotal) y conteo actual (usedTotal).
 * No es mutable: el incremento de usos se maneja fuera (en el "store"/repositorio).
 */
public record Coupon(
        String code,
        BigDecimal minAmountRequired, // monto mínimo para aplicar
        BigDecimal discountAmount,    // descuento fijo (puedes cambiarlo a % si quieres)
        int maxUsesTotal,             // cantidad máxima de usos totales
        int usedTotal                 // cantidad ya usada (0..maxUsesTotal)
) {
    public boolean hasRemainingUses() {
        return usedTotal < maxUsesTotal;
    }

    public int remainingUses() {
        return Math.max(0, maxUsesTotal - usedTotal);
    }
}
