package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingFeeInfo {
    @JsonProperty("FeeType")
    private String feeType;

    @JsonProperty("BookingCurrencyPrice")
    private double bookingCurrencyPrice;

    @JsonProperty("EquivCurrencyPrice")
    private double equivCurrencyPrice;

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

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