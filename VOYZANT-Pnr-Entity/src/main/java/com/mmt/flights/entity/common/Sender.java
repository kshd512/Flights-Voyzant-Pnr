package com.mmt.flights.entity.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sender {
    @JsonProperty("TravelAgencySender")
    private TravelAgencySender travelAgencySender;

    public TravelAgencySender getTravelAgencySender() {
        return travelAgencySender;
    }

    public void setTravelAgencySender(TravelAgencySender travelAgencySender) {
        this.travelAgencySender = travelAgencySender;
    }
}