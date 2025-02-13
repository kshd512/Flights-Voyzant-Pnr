package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PriceDetail {
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
}