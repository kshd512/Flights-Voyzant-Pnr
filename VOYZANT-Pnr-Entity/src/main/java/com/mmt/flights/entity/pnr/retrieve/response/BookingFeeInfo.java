package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BookingFeeInfo {
    @JsonProperty("FeeType")
    private String feeType;
    @JsonProperty("BookingCurrencyPrice")
    private Double bookingCurrencyPrice;
    @JsonProperty("EquivCurrencyPrice")
    private Double equivCurrencyPrice;

    public Double getBookingCurrencyPrice() {
        return bookingCurrencyPrice;
    }

    public Double getEquivCurrencyPrice() {
        return equivCurrencyPrice;
    }

    public void setEquivCurrencyPrice(Double equivCurrencyPrice) {
        this.equivCurrencyPrice = equivCurrencyPrice;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }
}