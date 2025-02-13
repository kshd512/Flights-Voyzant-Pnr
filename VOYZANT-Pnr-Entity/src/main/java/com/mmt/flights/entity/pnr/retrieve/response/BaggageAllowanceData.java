package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BaggageAllowanceData {
    @JsonProperty("SegmentRefs")
    private String segmentRefs;
    
    @JsonProperty("PassengerRefs")
    private String passengerRefs;
    
    @JsonProperty("BaggageAllowanceRef")
    private String baggageAllowanceRef;

    public String getSegmentRefs() {
        return segmentRefs;
    }

    public void setSegmentRefs(String segmentRefs) {
        this.segmentRefs = segmentRefs;
    }

    public String getPassengerRefs() {
        return passengerRefs;
    }

    public void setPassengerRefs(String passengerRefs) {
        this.passengerRefs = passengerRefs;
    }

    public String getBaggageAllowanceRef() {
        return baggageAllowanceRef;
    }

    public void setBaggageAllowanceRef(String baggageAllowanceRef) {
        this.baggageAllowanceRef = baggageAllowanceRef;
    }
}
