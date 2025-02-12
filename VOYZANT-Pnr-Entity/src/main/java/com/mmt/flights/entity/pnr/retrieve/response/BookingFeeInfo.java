package com.mmt.flights.entity.pnr.retrieve.response;

public class BookingFeeInfo {
    private String feeType;
    private PriceDetail price;

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public PriceDetail getPrice() {
        return price;
    }

    public void setPrice(PriceDetail price) {
        this.price = price;
    }
}