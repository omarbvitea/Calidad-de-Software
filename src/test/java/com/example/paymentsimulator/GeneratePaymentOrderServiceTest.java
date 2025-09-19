/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.paymentsimulator;
import com.example.paymentsimulator.domain.Bank;
import com.example.paymentsimulator.domain.Card;
import com.example.paymentsimulator.domain.PaymentOrder;
import com.example.paymentsimulator.exceptions.ValidationException;
import com.example.paymentsimulator.services.AmountValidator;
import com.example.paymentsimulator.services.CardValidator;
import com.example.paymentsimulator.services.CommissionService;
import com.example.paymentsimulator.services.CouponValidator;
import com.example.paymentsimulator.services.ImageValidator;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
 *
 * @author User
 */
public class GeneratePaymentOrderServiceTest {
    private GeneratePaymentOrderService newSut(
      ImageValidator img, AmountValidator amt, CardValidator card,
      CouponValidator coup, CommissionService com) {
    return new GeneratePaymentOrderService(img, amt, card, coup, com);
  }

  @Test
  void generate_happyPath() {
    ImageValidator imageValidator = mock(ImageValidator.class);
    AmountValidator amountValidator = mock(AmountValidator.class);
    CardValidator cardValidator = mock(CardValidator.class);
    CouponValidator couponValidator = mock(CouponValidator.class);
    CommissionService commissionService = mock(CommissionService.class);

    // datos
    List<Card> cards = List.of(new Card("****-1234", Bank.BCP));
    String chosen = "****-1234";

    // stubs
    when(cardValidator.validate(cards, chosen, Bank.BCP)).thenReturn(cards.get(0));
    when(couponValidator.validate(anyList(), anyList(), eq(new BigDecimal("100.00"))))
        .thenReturn(List.of()); // no necesitamos objetos Coupon reales
    when(couponValidator.totalDiscount(any())).thenReturn(new BigDecimal("10.00")); // descuento fijo
    when(commissionService.commissionOf(new BigDecimal("90.00")))
        .thenReturn(new BigDecimal("4.50")); // comisión sobre (100-10)

    GeneratePaymentOrderService sut = newSut(
        imageValidator, amountValidator, cardValidator, couponValidator, commissionService);

    // act
    PaymentOrder order = sut.generate(
        "voucher.png",
        new BigDecimal("100.00"),
        "PEN",
        cards,
        chosen,
        Bank.BCP,
        List.of(),              // available coupons
        List.of("DESC10")       // codes
    );

    // asserts de estado
    assertNotNull(order);
    assertEquals(new BigDecimal("100.00"), order.originalAmount().amount());
    assertEquals("PEN", order.originalAmount().currency());
    assertEquals(new BigDecimal("10.00"), order.discountTotal().amount());
    assertEquals(new BigDecimal("4.50"),  order.commission().amount());
    assertEquals(new BigDecimal("94.50"), order.finalAmount().amount());
    assertEquals("****-1234", order.card().maskedNumber());
    assertTrue(order.orderCode().startsWith("ORD-"));

    // asserts de interacción (simples, sin InOrder)
    verify(imageValidator).validate("voucher.png");
    verify(amountValidator).validate(new BigDecimal("100.00"));
    verify(cardValidator).validate(cards, chosen, Bank.BCP);
    verify(couponValidator).validate(anyList(), anyList(), eq(new BigDecimal("100.00")));
    verify(couponValidator).totalDiscount(any());
    verify(commissionService).commissionOf(new BigDecimal("90.00"));
    verifyNoMoreInteractions(imageValidator, amountValidator, cardValidator, couponValidator, commissionService);
  }

  @Test
  void generate_cortaAnteImagenInvalida() {
    ImageValidator imageValidator = mock(ImageValidator.class);
    AmountValidator amountValidator = mock(AmountValidator.class);
    CardValidator cardValidator = mock(CardValidator.class);
    CouponValidator couponValidator = mock(CouponValidator.class);
    CommissionService commissionService = mock(CommissionService.class);

    doThrow(new ValidationException("ext inválida"))
        .when(imageValidator).validate("bad.gif");

    GeneratePaymentOrderService sut = newSut(
        imageValidator, amountValidator, cardValidator, couponValidator, commissionService);

    assertThrows(ValidationException.class, () ->
        sut.generate("bad.gif", new BigDecimal("10.00"), "PEN",
            List.of(new Card("****-1234", Bank.BCP)), "****-1234",
            Bank.BCP, List.of(), List.of())
    );

    // no toca nada más si falla la imagen
    verifyNoInteractions(amountValidator, cardValidator, couponValidator, commissionService);
  }  
}
