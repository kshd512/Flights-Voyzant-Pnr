package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BookingFeeInfo {
    @JsonProperty("FeeType")
    private String feeType;
    
    private Double bookingCurrencyPrice;
    
    private Double equivCurrencyPrice;

    @JsonProperty("BookingCurrencyPrice")
    public void setBookingCurrencyPrice(String value) {
        if (!"NA".equalsIgnoreCase(value)) {    
            this.bookingCurrencyPrice = Double.valueOf(value);
        }
    }

    @JsonProperty("EquivCurrencyPrice")
    public void setEquivCurrencyPrice(String value) {
        if (!"NA".equalsIgnoreCase(value)) {
            this.equivCurrencyPrice = Double.valueOf(value);
        }
    }

    public Double getBookingCurrencyPrice() {
        return bookingCurrencyPrice;
    }

    public Double getEquivCurrencyPrice() {
        return equivCurrencyPrice;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }
}