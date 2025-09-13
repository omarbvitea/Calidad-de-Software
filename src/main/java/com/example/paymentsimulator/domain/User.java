package com.example.paymentsimulator.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Usuario registrado en el sistema.
 */
public record User(
        String id,
        String name,
        String email,
        BigDecimal commissionRate,    // tasa personalizada (puede ser null si aplica la general)
        BigDecimal creditLimit,       // saldo/crédito máximo permitido
        BigDecimal currentConsumption // consumo actual
) {
    public User {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(creditLimit, "creditLimit");
        Objects.requireNonNull(currentConsumption, "currentConsumption");
    }

    public boolean hasAvailableCredit(BigDecimal amount) {
        return currentConsumption.add(amount).compareTo(creditLimit) <= 0;
    }
}
