package com.example.demo.entity;

public enum CarStatus {
    AVAILABLE("可租"),
    RENTED("已租"),
    MAINTENANCE("维修");

    private final String dbValue;

    CarStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static CarStatus fromPersistenceValue(String value) {
        for (CarStatus status : values()) {
            if (status.name().equalsIgnoreCase(value) || status.dbValue.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown car status: " + value);
    }
}
