package com.example.paymentsimulator;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PaymentSimulatorTest {

    // Creamos una interfaz de ejemplo para simular dependencia
    interface PaymentService {
        String process(String userId);
    }

    @Test
    void testMockitoWorks() {
        // Crea un mock de PaymentService
        PaymentService paymentService = Mockito.mock(PaymentService.class);

        // Configura comportamiento simulado
        when(paymentService.process("123")).thenReturn("Pago exitoso");

        // Llamar al mock
        String result = paymentService.process("123");

        // Validar que devuelva lo esperado
        assertEquals("Pago exitoso", result);

        // Verificar que se llamó una vez
        verify(paymentService, times(1)).process("123");
    }
}
