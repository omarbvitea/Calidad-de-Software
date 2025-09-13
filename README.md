# 💳 PaymentSimulator

**PaymentSimulator** es un proyecto de ejemplo en **Java 24 + Maven** que simula el flujo de generación de órdenes de pago con validaciones de usuario, tarjetas, cupones de descuento y comisiones.

Este proyecto fue creado con fines educativos y de práctica, usando archivos `.txt` como una base de datos simulada.

---

## ✨ Features

- ✅ Validación de imagen adjunta (`.jpg`, `.jpeg`, `.png`)
- ✅ Validación de monto dentro de un rango
- ✅ Validación de tarjeta (usuario, banco, dígitos)
- ✅ Validación de cupones (existencia, mínimo de compra, usos disponibles, máximo 3)
- ✅ Cálculo automático de comisión según monto
- ✅ Generación de código de orden único
- ✅ Persistencia simulada con archivos `.txt` en `resources/data`

---

## 📂 Estructura del Proyecto

```bash
src/main/java/com/example/paymentsimulator/
├─ PaymentSimulator.java # Main con demo de flujo
├─ application/ # Orquestador del caso de uso
│ └─ GeneratePaymentOrderService.java
├─ domain/ # Entidades y VOs
│ ├─ Bank.java
│ ├─ Card.java
│ ├─ Coupon.java
│ ├─ Money.java
│ ├─ PaymentOrder.java
│ ├─ User.java
│ └─ UserCard.java
├─ exceptions/ # Excepciones personalizadas
│ ├─ BusinessException.java
│ └─ ValidationException.java
├─ services/ # Validaciones y lógica de negocio
│ ├─ ImageValidator.java
│ ├─ AmountValidator.java
│ ├─ CardValidator.java
│ ├─ CouponValidator.java
│ └─ CommissionService.java
└─ store/ # Persistencia simulada en TXT
├─ UserTxtStore.java
├─ UserCardTxtStore.java
└─ CouponTxtStore.java
```

---

## 📑 Data simulada

Los datos se encuentran en `src/main/resources/data/`.
- `users.txt`: Usuarios con ID, nombre y email.
- `user_cards.txt`: Tarjetas asociadas a usuarios con detalles de banco y díg
itos.
- `coupons.txt`: Cupones con código, descuento, mínimo de compra y usos

---

## 🚀 Cómo ejecutar

1. Clonar el repositorio:

```bash
   git clone https://github.com/tu-usuario/PaymentSimulator.git
   cd PaymentSimulator
```

2. Compilar y ejecutar:

```bash
    mvn clean package
```

3. Ejecutar la clase principal:

```bash
    mvn exec:java -Dexec.mainClass="com.example.paymentsimulator.PaymentSimulator"
```

## 🧪 Próximos pasos

- Añadir tests unitarios con JUnit 5 + Mockito
- Mejorar validación de usuarios con reglas de crédito
- Persistencia en SQLite/H2 en vez de TXT
- API REST con Spring Boot

## 📜 Licencia

Este proyecto es de uso educativo y no debe usarse en entornos de producción.

Los números de tarjeta son falsos y se utilizan únicamente para fines de simulación.