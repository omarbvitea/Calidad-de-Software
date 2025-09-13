package com.example.paymentsimulator.exceptions;

/**
 * Excepción para errores de negocio.
 * Ejemplo: cupón ya agotado, usuario sin crédito disponible, etc.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
