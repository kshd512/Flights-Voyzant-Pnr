package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class FareDetail {
    @JsonProperty("PassengerRefs")
    private String passengerRefs;
    @JsonProperty("Price")
    private Price price;

    public String getPassengerRefs() {
        return passengerRefs;
    }

    public void setPassengerRefs(String passengerRefs) {
        this.passengerRefs = passengerRefs;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }
}