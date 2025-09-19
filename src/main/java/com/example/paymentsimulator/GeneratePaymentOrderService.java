package com.example.paymentsimulator;

import com.example.paymentsimulator.domain.Bank;
import com.example.paymentsimulator.domain.Card;
import com.example.paymentsimulator.domain.Coupon;
import com.example.paymentsimulator.domain.Money;
import com.example.paymentsimulator.domain.PaymentOrder;
import com.example.paymentsimulator.services.AmountValidator;
import com.example.paymentsimulator.services.CardValidator;
import com.example.paymentsimulator.services.CommissionService;
import com.example.paymentsimulator.services.CouponValidator;
import com.example.paymentsimulator.services.ImageValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Orquestador del caso de uso "Generar orden de pago".
 * - Valida imagen (.jpg/.jpeg/.png por nombre)
 * - Valida monto en rango
 * - Valida tarjeta (pertenece al usuario, dígitos, banco)
 * - Valida cupones (existencia, mínimo, usos disponibles, máx 3)
 * - Calcula descuento, comisión por tramos y total
 * - Genera un código de orden simple (puedes reemplazar por un generador real)
 */
public class GeneratePaymentOrderService {

    private final ImageValidator imageValidator;
    private final AmountValidator amountValidator;
    private final CardValidator cardValidator;
    private final CouponValidator couponValidator;
    private final CommissionService commissionService;

    public GeneratePaymentOrderService(ImageValidator imageValidator,
                                       AmountValidator amountValidator,
                                       CardValidator cardValidator,
                                       CouponValidator couponValidator,
                                       CommissionService commissionService) {
        this.imageValidator = imageValidator;
        this.amountValidator = amountValidator;
        this.cardValidator = cardValidator;
        this.couponValidator = couponValidator;
        this.commissionService = commissionService;
    }

    /**
     * Genera una orden de pago.
     *
     * @param fileName        nombre del archivo de imagen (se valida extensión)
     * @param amount          monto de la orden (antes de descuentos)
     * @param currency        moneda (por ejemplo: "PEN", "USD")
     * @param userCards       tarjetas registradas del usuario
     * @param chosenMasked    tarjeta elegida (maskedNumber)
     * @param bank            banco seleccionado por el usuario
     * @param availableCoupons lista de cupones disponibles (por ejemplo: cargados desde TXT)
     * @param couponCodes     códigos de cupón ingresados por el usuario (máx 3)
     * @return PaymentOrder construida
     */
    public PaymentOrder generate(String fileName,
                                 BigDecimal amount,
                                 String currency,
                                 List<Card> userCards,
                                 String chosenMasked,
                                 Bank bank,
                                 List<Coupon> availableCoupons,
                                 List<String> couponCodes) {

        // Validaciones
        imageValidator.validate(fileName);
        amountValidator.validate(amount);
        Card card = cardValidator.validate(userCards, chosenMasked, bank);
        List<Coupon> coupons = couponValidator.validate(availableCoupons, couponCodes, amount);

        // Cálculos 
        BigDecimal discount = couponValidator.totalDiscount(coupons);
        BigDecimal base = amount.subtract(discount);
        if (base.signum() < 0) base = BigDecimal.ZERO;

        BigDecimal commission = commissionService.commissionOf(base);
        BigDecimal total = base.add(commission);

        // Construcción de Money y orden
        Money original = new Money(amount, currency);
        Money discountMoney = new Money(discount, currency);
        Money commissionMoney = new Money(commission, currency);
        Money finalMoney = new Money(total, currency);

        String orderCode = genOrderCode(); 

        return new PaymentOrder(
                orderCode,
                original,
                discountMoney,
                commissionMoney,
                finalMoney,
                card,
                coupons.stream().map(Coupon::code).toList(),
                Instant.now()
        );
    }

    /** Generador simple de códigos (por ejemplo: ORD-1712778890123). */
    private String genOrderCode() {
        return "ORD-" + System.currentTimeMillis();
    }
}
