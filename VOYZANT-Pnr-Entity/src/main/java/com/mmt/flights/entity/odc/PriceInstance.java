package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceInstance {

    @JsonProperty("BookingCurrencyPrice")
    private double bookingCurrencyPrice;

    @JsonProperty("EquivCurrencyPrice")
    private double equivCurrencyPrice;

    public double getBookingCurrencyPrice() {
        return bookingCurrencyPrice;
    }

    public void setBookingCurrencyPrice(double bookingCurrencyPrice) {
        this.bookingCurrencyPrice = bookingCurrencyPrice;
    }

    public double getEquivCurrencyPrice() {
        return equivCurrencyPrice;
    }

    public void setEquivCurrencyPrice(double equivCurrencyPrice) {
        this.equivCurrencyPrice = equivCurrencyPrice;
    }
}
