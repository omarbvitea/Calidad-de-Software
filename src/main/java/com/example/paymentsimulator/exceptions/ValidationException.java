package com.example.paymentsimulator.exceptions;

/**
 * Excepción para errores de validación de entrada o reglas simples.
 * Ejemplo: monto fuera de rango, formato de archivo incorrecto, etc.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}

