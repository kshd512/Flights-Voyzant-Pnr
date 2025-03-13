package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaggageAllowance {
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