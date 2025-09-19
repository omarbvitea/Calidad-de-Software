/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.paymentsimulator;
import com.example.paymentsimulator.domain.Bank;
import com.example.paymentsimulator.domain.Card;
import com.example.paymentsimulator.domain.Coupon;
import com.example.paymentsimulator.domain.PaymentOrder;
import com.example.paymentsimulator.exceptions.ValidationException;
import com.example.paymentsimulator.services.AmountValidator;
import com.example.paymentsimulator.services.CardValidator;
import com.example.paymentsimulator.services.CommissionService;
import com.example.paymentsimulator.services.CouponValidator;
import com.example.paymentsimulator.services.ImageValidator;

import org.junit.jupiter.api.Test;

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
  void generate_FlujoExitoso() {  
    //Valida que el servicio crea una orden válida de principio a fin, 
    //coordinando con los colaboradores y dando un resultado esperado al usuario.
     
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
    //Comprueba que, si el comprobante de pago no es válido, el proceso se detiene al inicio y
    //no sigue con pasos innecesarios.
    
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
   @Test
  void generate_cortaSiTarjetaInvalida_noValidaCuponesNiComisiona() {
    //Verifica que, si la tarjeta elegida no corresponde, el sistema avisa y no 
    //intenta aplicar beneficios ni costos; corta de forma segura.
    
    ImageValidator image = mock(ImageValidator.class);
    AmountValidator amount = mock(AmountValidator.class);
    CardValidator card = mock(CardValidator.class);
    CouponValidator coupon = mock(CouponValidator.class);
    CommissionService commission = mock(CommissionService.class);

    List<Card> cards = List.of(new Card("****-9999", Bank.BCP));

    doNothing().when(image).validate("ok.png");
    doNothing().when(amount).validate(new BigDecimal("50.00"));
    doThrow(new ValidationException("tarjeta no válida"))
        .when(card).validate(cards, "****-9999", Bank.BCP);

    GeneratePaymentOrderService sut = newSut(image, amount, card, coupon, commission);

    assertThrows(ValidationException.class, () ->
        sut.generate("ok.png", new BigDecimal("50.00"), "PEN",
            cards, "****-9999", Bank.BCP, List.of(), List.of())
    );

    verifyNoInteractions(coupon, commission);
  }
  
  @Test
  void generate_pasaBaseCorrectaAComision() {
    //Verifica que el costo adicional se calcula solo sobre lo que realmente queda por pagar
    // y que el total final refleja esa lógica con sentido común.
    
    ImageValidator image = mock(ImageValidator.class);
    AmountValidator amount = mock(AmountValidator.class);
    CardValidator card = mock(CardValidator.class);
    CouponValidator coupon = mock(CouponValidator.class);
    CommissionService commission = mock(CommissionService.class);

    List<Card> cards = List.of(new Card("****-1234", Bank.BCP));
    when(card.validate(cards, "****-1234", Bank.BCP)).thenReturn(cards.get(0));

    when(coupon.validate(anyList(), anyList(), eq(new BigDecimal("100.00"))))
        .thenReturn(List.of(new Coupon("D25", new BigDecimal("0.00"),
            new BigDecimal("25.00"), 10, 0)));
    when(coupon.totalDiscount(any())).thenReturn(new BigDecimal("25.00"));

    // Verifica con compareTo (evita problemas de escala 0 vs 0.00)
    when(commission.commissionOf(argThat(bd -> bd.compareTo(new BigDecimal("75.00")) == 0)))
        .thenReturn(new BigDecimal("3.75"));

    GeneratePaymentOrderService sut = newSut(image, amount, card, coupon, commission);

    PaymentOrder order = sut.generate(
        "ok.png", new BigDecimal("100.00"), "PEN",
        cards, "****-1234", Bank.BCP, List.of(), List.of("D25"));

    assertEquals(new BigDecimal("78.75"), order.finalAmount().amount()); // 75 + 3.75
    verify(commission).commissionOf(argThat(bd -> bd.compareTo(new BigDecimal("75.00")) == 0));
  }
}
