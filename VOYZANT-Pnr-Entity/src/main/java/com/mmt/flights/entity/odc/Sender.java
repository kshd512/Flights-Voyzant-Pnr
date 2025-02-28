package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
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