package com.example.paymentsimulator.domain;

/**
 * Relación entre un usuario y una tarjeta registrada.
 */
public record UserCard(
        String userId,
        Card card
) {
}