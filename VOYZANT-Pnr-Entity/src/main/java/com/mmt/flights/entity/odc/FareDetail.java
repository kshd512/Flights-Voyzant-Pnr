package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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