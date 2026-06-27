package com.example.demo.entity;

public enum RentalStatus {
    PENDING_PAYMENT("待支付"),
    RENTING("租赁中"),
    COMPLETED("已完成"),
    CANCELED("已取消");

    private final String dbValue;

    RentalStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static RentalStatus fromPersistenceValue(String value) {
        for (RentalStatus status : values()) {
            if (status.name().equalsIgnoreCase(value) || status.dbValue.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown rental status: " + value);
    }
}
