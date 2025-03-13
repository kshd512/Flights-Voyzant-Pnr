package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceDetail {
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
}