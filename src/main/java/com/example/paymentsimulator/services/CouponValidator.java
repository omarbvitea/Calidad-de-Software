package com.example.paymentsimulator.services;

import com.example.paymentsimulator.domain.Coupon;
import com.example.paymentsimulator.exceptions.BusinessException;
import com.example.paymentsimulator.exceptions.ValidationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Valida cupones contra una lista disponible (p.ej. cargada desde TXT).
 * - Máximo 3 códigos simultáneos.
 * - Deben existir.
 * - Deben cumplir monto mínimo.
 * - Deben tener usos disponibles (hasRemainingUses).
 */
public class CouponValidator {

    public List<Coupon> validate(List<Coupon> availableCoupons,
                                 List<String> codes,
                                 BigDecimal orderAmount) {

        if (codes == null || codes.isEmpty()) return List.of();
        if (codes.size() > 3) throw new ValidationException("Máximo 3 cupones");

        List<Coupon> valid = new ArrayList<>();
        for (String code : codes) {
            Coupon coupon = availableCoupons.stream()
                    .filter(c -> c.code().equals(code))
                    .findFirst()
                    .orElseThrow(() -> new ValidationException("Cupón no existe: " + code));

            if (orderAmount.compareTo(coupon.minAmountRequired()) < 0)
                throw new ValidationException("Monto no cumple mínimo para " + code);

            if (!coupon.hasRemainingUses())
                throw new BusinessException("Cupón sin usos disponibles: " + code);

            valid.add(coupon);
        }
        return valid;
    }

    /** Suma de descuentos fijos. Ajusta si luego usas porcentaje. */
    public BigDecimal totalDiscount(List<Coupon> coupons) {
        return coupons == null ? BigDecimal.ZERO :
                coupons.stream()
                        .map(Coupon::discountAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
