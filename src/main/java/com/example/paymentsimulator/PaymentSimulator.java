package com.example.paymentsimulator;

import com.example.paymentsimulator.domain.*;
import com.example.paymentsimulator.exceptions.ValidationException;
import com.example.paymentsimulator.services.*;
import com.example.paymentsimulator.store.CouponTxtStore;
import com.example.paymentsimulator.store.UserCardTxtStore;
import com.example.paymentsimulator.store.UserTxtStore;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class PaymentSimulator {

    // Helper para resolver rutas de resources (data/*.txt)
    private static String resourcePath(String resource) {
        try {
            URL url = Objects.requireNonNull(
                    PaymentSimulator.class.getClassLoader().getResource(resource),
                    "No se encontró el recurso: " + resource
            );
            return Paths.get(url.toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error resolviendo resource " + resource, e);
        }
    }

    public static void main(String[] args) {
        try {
            // 1) Rutas de data (desde resources)
            String usersPath = resourcePath("data/users.txt");
            String userCardsPath = resourcePath("data/user_cards.txt");
            String couponsPath = resourcePath("data/coupons.txt");

            // 2) Stores
            UserTxtStore userStore = new UserTxtStore(usersPath);
            UserCardTxtStore userCardStore = new UserCardTxtStore(userCardsPath);
            CouponTxtStore couponStore = new CouponTxtStore(couponsPath);

            // 3) Configuración de validadores y servicio
            ImageValidator imageValidator = new ImageValidator();
            AmountValidator amountValidator = new AmountValidator(new BigDecimal("1.00"), new BigDecimal("1000000"));
            CardValidator cardValidator = new CardValidator();
            CouponValidator couponValidator = new CouponValidator();
            CommissionService commissionService = new CommissionService();

            GeneratePaymentOrderService service = new GeneratePaymentOrderService(
                    imageValidator, amountValidator, cardValidator, couponValidator, commissionService
            );

            // 4) Datos de la “petición”
            String userId = "USER00001";
            String fileName = "voucher.png";
            BigDecimal amount = new BigDecimal("1500.00");
            String currency = "PEN";
            Bank selectedBank = Bank.BCP;
            String chosenMasked = "4349****1234";
            List<String> couponCodes = List.of("NUEVOCLI");

            // 5) Cargar tarjetas del usuario
            List<UserCard> userCards = userCardStore.findCardsByUser(userId);
            List<Card> cards = userCards.stream().map(UserCard::card).toList();

            // 6) Cargar cupones requeridos (de la lista de códigos)
            //    Nota: nuestro CouponValidator requiere "availableCoupons".
            //    Para esta demo, cargamos SOLO los cupones que el usuario ingresó.
            List<Coupon> availableCoupons = couponCodes.stream()
                    .map(code -> couponStore.findByCode(code)
                            .orElseThrow(() -> new ValidationException("Cupón no existe: " + code)))
                    .toList();

            // 7) Ejecutar el caso de uso
            PaymentOrder order = service.generate(
                    fileName,
                    amount,
                    currency,
                    cards,
                    chosenMasked,
                    selectedBank,
                    availableCoupons,
                    couponCodes
            );

            // 8) Imprimir resultado
            System.out.println("==== ORDEN GENERADA ====");
            System.out.println("Código        : " + order.orderCode());
            System.out.println("Monto original: " + order.originalAmount().amount() + " " + order.originalAmount().currency());
            System.out.println("Descuento     : " + order.discountTotal().amount());
            System.out.println("Comisión      : " + order.commission().amount());
            System.out.println("Total         : " + order.finalAmount().amount() + " " + order.finalAmount().currency());
            System.out.println("Tarjeta       : " + order.card().maskedNumber() + " (" + order.card().bank() + ")");
            System.out.println("Cupones       : " + order.appliedCoupons());
            System.out.println("Creada en     : " + order.createdAt());

        } catch (Exception e) {
            System.err.println("Error en el flujo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
