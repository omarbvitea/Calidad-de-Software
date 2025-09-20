package com.example.paymentsimulator.services;

import com.example.paymentsimulator.domain.Coupon;
import com.example.paymentsimulator.exceptions.BusinessException;
import com.example.paymentsimulator.exceptions.ValidationException;
import com.example.paymentsimulator.services.CouponValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CouponValidatorTest {

    private final CouponValidator sut = new CouponValidator();

    @Test
    void validar_retornaListaVaciaCuandoNoHayCupones() {
        List<Coupon> result = sut.validate(List.of(), null, new BigDecimal("100.00"));
        assertTrue(result.isEmpty());
    }

    @Test
    void validar_ExcepcionCuandoHayMasDeTresCodigos() {
        List<String> codes = List.of("C1", "C2", "C3", "C4");
        ValidationException ex = assertThrows(ValidationException.class, () ->
                sut.validate(List.of(), codes, new BigDecimal("100.00"))
        );
        assertTrue(ex.getMessage().contains("Máximo 3 cupones"));
    }

    @Test
    void validar_ExcepcionCuandoCuponNoExiste() {
        List<String> codes = List.of("NO_EXISTE");
        ValidationException ex = assertThrows(ValidationException.class, () ->
                sut.validate(List.of(), codes, new BigDecimal("100.00"))
        );
        assertTrue(ex.getMessage().contains("Cupón no existe"));
    }

    @Test
    void validar_ExcepcionCuandoMontoMinimoNoCumplido() {
        Coupon coupon = new Coupon("DESC10", new BigDecimal("50"), new BigDecimal("10"), 10, 0);
        List<Coupon> available = List.of(coupon);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                sut.validate(available, List.of("DESC10"), new BigDecimal("30"))
        );
        assertTrue(ex.getMessage().contains("Monto no cumple mínimo"));
    }

    @Test
    void validar_ExcepcionCuandoCuponNoTieneUsos() {
        Coupon coupon = new Coupon("DESC10", BigDecimal.ZERO, new BigDecimal("10"), 0, 0); // sin usos
        List<Coupon> available = List.of(coupon);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                sut.validate(available, List.of("DESC10"), new BigDecimal("100"))
        );
        assertTrue(ex.getMessage().contains("sin usos disponibles"));
    }

    @Test
    void validar_RetornaCuponValido() {
        Coupon coupon = new Coupon("DESC10", new BigDecimal("10"), new BigDecimal("50"), 5, 0);
        List<Coupon> available = List.of(coupon);

        List<Coupon> result = sut.validate(available, List.of("DESC10"), new BigDecimal("100"));
        assertEquals(1, result.size());
        assertEquals("DESC10", result.get(0).code());
    }

    @Test
    void validar_SumaTodosLosDescuentos() {
        Coupon c1 = new Coupon("A", BigDecimal.ZERO, new BigDecimal("10"), 5, 0);
        Coupon c2 = new Coupon("B", BigDecimal.ZERO, new BigDecimal("5"),  5, 0);

        BigDecimal total = sut.totalDiscount(List.of(c1, c2));

        assertEquals(new BigDecimal("15"), total);
    }

    @Test
    void validar_SinDescuentoCuandoNoHayCupones() {
        BigDecimal total = sut.totalDiscount(null);
        assertEquals(BigDecimal.ZERO, total);
    }
}
