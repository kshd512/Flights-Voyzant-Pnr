package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Flight {
    @JsonProperty("FlightKey")
    private String flightKey;
    
    @JsonProperty("Journey")
    private Journey journey;
    
    @JsonProperty("SegmentReferences")
    private String segmentReferences;

    public String getFlightKey() {
        return flightKey;
    }

    public void setFlightKey(String flightKey) {
        this.flightKey = flightKey;
    }

    public Journey getJourney() {
        return journey;
    }

    public void setJourney(Journey journey) {
        this.journey = journey;
    }

    public String getSegmentReferences() {
        return segmentReferences;
    }

    public void setSegmentReferences(String segmentReferences) {
        this.segmentReferences = segmentReferences;
    }
}