package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BookingFeeInfo {
    @JsonProperty("FeeType")
    private String feeType;
    @JsonProperty("Price")
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