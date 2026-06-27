package com.example.demo;

import com.example.demo.entity.CarStatus;
import com.example.demo.entity.PaymentMethod;
import com.example.demo.entity.RentalStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityEnumMappingTests {

    @Test
    void carStatusShouldSupportEnglishAndChineseValues() {
        assertEquals("AVAILABLE", CarStatus.AVAILABLE.name());
        assertEquals("可租", CarStatus.AVAILABLE.getDbValue());
        assertEquals(CarStatus.RENTED, CarStatus.fromPersistenceValue("RENTED"));
        assertEquals(CarStatus.RENTED, CarStatus.fromPersistenceValue("已租"));
    }

    @Test
    void rentalStatusShouldSupportEnglishAndChineseValues() {
        assertEquals("PENDING_PAYMENT", RentalStatus.PENDING_PAYMENT.name());
        assertEquals("待支付", RentalStatus.PENDING_PAYMENT.getDbValue());
        assertEquals(RentalStatus.COMPLETED, RentalStatus.fromPersistenceValue("COMPLETED"));
        assertEquals(RentalStatus.COMPLETED, RentalStatus.fromPersistenceValue("已完成"));
    }

    @Test
    void paymentMethodShouldSupportEnglishAndChineseValues() {
        assertEquals("WECHAT", PaymentMethod.WECHAT.name());
        assertEquals("微信", PaymentMethod.WECHAT.getDbValue());
        assertEquals(PaymentMethod.BANK_CARD, PaymentMethod.fromPersistenceValue("BANK_CARD"));
        assertEquals(PaymentMethod.BANK_CARD, PaymentMethod.fromPersistenceValue("银行卡"));
    }
}
