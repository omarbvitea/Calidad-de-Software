package com.example.paymentsimulator.services;

import com.example.paymentsimulator.exceptions.ValidationException;

import java.math.BigDecimal;

/**
 * Valida que el monto esté dentro de un rango [min, max].
 */
public class AmountValidator {
    private final BigDecimal min;
    private final BigDecimal max;

    public AmountValidator(BigDecimal min, BigDecimal max) {
        if (min == null || max == null || min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Rango inválido");
        }
        this.min = min;
        this.max = max;
    }

    public void validate(BigDecimal amount) {
        if (amount == null || amount.compareTo(min) < 0 || amount.compareTo(max) > 0) {
            throw new ValidationException("Monto fuera de rango [" + min + "," + max + "]");
        }
    }

    public BigDecimal min() { return min; }
    public BigDecimal max() { return max; }
}
