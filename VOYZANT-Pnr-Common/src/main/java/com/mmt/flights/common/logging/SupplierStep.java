package com.mmt.flights.common.logging;

public enum SupplierStep {
    ORDER_DETAIL("ORDER_DETAIL"),
    VOID_CANCEL_ORDER("VOID_CANCEL_ORDER"),
    CANCEL_ORDER("CANCEL_ORDER"),
    CHECK_REFUND("CHECK_REFUND"),
    CANCEL_RELEASE("CANCEL_RELEASE"),
    SPLIT_ORDER("SPLIT_ORDER"),
    ORDER_RESHOP("ORDER_RESHOP"),
    ORDER_EXCHANGE_PRICE("ORDER_EXCHANGE_PRICE"),
    ORDER_CHANGE_BOOK("ORDER_CHANGE_BOOK"),
    VOID_ORDER("VOID_ORDER");

    private final String value;

    SupplierStep(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}