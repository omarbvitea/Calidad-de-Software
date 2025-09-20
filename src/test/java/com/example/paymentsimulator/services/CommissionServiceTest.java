package com.example.paymentsimulator.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CommissionServiceTest {

  private CommissionRuleProvider provider; 
  private CommissionService service;     

  @BeforeEach
  void setUp() {
    provider = mock(CommissionRuleProvider.class);
    service  = new CommissionService(provider);
  }

  @Test
  void aplica_porcentaje_retorno_de_regla() {
    // base: 100.00, regla: 5%  => comisión: 5.00
    when(provider.percentFor(new BigDecimal("100.00")))
        .thenReturn(new BigDecimal("0.05"));

    BigDecimal commission = service.commissionOf(new BigDecimal("100.00"));

    assertEquals(new BigDecimal("5.00"), commission);
  }

  @Test
  void cambia_por_tramos_o_montos() {
    // tramo bajo: 50.00 -> 10%; tramo alto: 1000.00 -> 2%
    when(provider.percentFor(new BigDecimal("50.00")))
        .thenReturn(new BigDecimal("0.10"));
    when(provider.percentFor(new BigDecimal("1000.00")))
        .thenReturn(new BigDecimal("0.02"));

    BigDecimal c1 = service.commissionOf(new BigDecimal("50.00"));    // 5.00
    BigDecimal c2 = service.commissionOf(new BigDecimal("1000.00"));  // 20.00

    assertEquals(new BigDecimal("5.00"), c1);
    assertEquals(new BigDecimal("20.00"), c2);
  }

  @Test
  void redondea_a_dos_decimales() {
    // 3.333…% de 123.45 ≈ 4.115 → 4.12 (HALF_UP)
    when(provider.percentFor(new BigDecimal("123.45")))
        .thenReturn(new BigDecimal("0.0333333"));

    BigDecimal commission = service.commissionOf(new BigDecimal("123.45"));

    assertEquals(new BigDecimal("4.12"), commission);
    verify(provider).percentFor(new BigDecimal("123.45"));
  }
}
