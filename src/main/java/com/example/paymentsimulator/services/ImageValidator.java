package com.example.paymentsimulator.services;

import com.example.paymentsimulator.exceptions.ValidationException;

/**
 * Valida que el nombre de archivo termine en .jpg o .png (case-insensitive).
 */
public class ImageValidator {

    public void validate(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new ValidationException("El nombre de archivo no puede ser vacío");
        }
        String lower = fileName.toLowerCase().trim();
        if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png"))) {
            throw new ValidationException("Formato de imagen no permitido: " + fileName);
        }
    }
}
