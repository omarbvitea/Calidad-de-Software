package com.example.paymentsimulator.services;

import com.example.paymentsimulator.domain.Bank;
import com.example.paymentsimulator.domain.Card;
import com.example.paymentsimulator.exceptions.ValidationException;

import java.util.List;

public class CardValidator {

    public Card validate(List<Card> userCards, String maskedNumber, Bank selectedBank) {
        if (userCards == null || userCards.isEmpty()) {
            throw new ValidationException("El usuario no tiene tarjetas registradas");
        }

        return userCards.stream()
                .filter(c -> c.maskedNumber().equals(maskedNumber))
                .findFirst()
                .map(c -> {
                    String num = c.maskedNumber();

                    if (num.length() < 8) {
                        throw new ValidationException("Formato de tarjeta inválido: demasiado corto");
                    }

                    String prefix = num.substring(0, 4);
                    String suffix = num.substring(num.length() - 4);

                    if (!prefix.matches("\\d{4}")) {
                        throw new ValidationException("Los primeros 4 dígitos deben ser numéricos");
                    }
                    if (!suffix.matches("\\d{4}")) {
                        throw new ValidationException("Los últimos 4 dígitos deben ser numéricos");
                    }

                    if (!c.bank().equals(selectedBank)) {
                        throw new ValidationException("La tarjeta no corresponde al banco seleccionado");
                    }

                    return c;
                })
                .orElseThrow(() -> new ValidationException("Tarjeta no pertenece al usuario"));
    }
}
