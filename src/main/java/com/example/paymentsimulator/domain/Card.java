package com.example.paymentsimulator.domain;

public record Card(String maskedNumber, Bank bank) {
    /**
     * Retorna la cantidad de dígitos numéricos de la tarjeta.
     * Ejemplo: "****1234" -> 4
     */
    public int digits() {
        return maskedNumber.replaceAll("\\D", "").length();
    }
}
