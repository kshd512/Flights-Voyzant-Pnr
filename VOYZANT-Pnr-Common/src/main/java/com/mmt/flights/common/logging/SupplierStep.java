package com.mmt.flights.common.logging;

public enum SupplierStep {
    ORDER_DETAIL("ORDER_DETAIL"),
    VOID_CANCEL_ORDER("VOID_CANCEL_ORDER"),
    CANCEL_ORDER("CANCEL_ORDER"),
    CHECK_REFUND("CHECK_REFUND"),
    CANCEL_RELEASE("CANCEL_RELEASE"),
    SPLIT_ORDER("SPLIT_ORDER"),
    ORDER_RESHOP("ORDER_RESHOP");  // Adding ORDER_RESHOP step

    private final String value;

    SupplierStep(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}