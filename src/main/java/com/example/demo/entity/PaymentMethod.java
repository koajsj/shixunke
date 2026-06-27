package com.example.demo.entity;

public enum PaymentMethod {
    WECHAT("微信"),
    ALIPAY("支付宝"),
    BANK_CARD("银行卡"),
    CASH("现金");

    private final String dbValue;

    PaymentMethod(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static PaymentMethod fromPersistenceValue(String value) {
        for (PaymentMethod method : values()) {
            if (method.name().equalsIgnoreCase(value) || method.dbValue.equals(value)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown payment method: " + value);
    }
}
