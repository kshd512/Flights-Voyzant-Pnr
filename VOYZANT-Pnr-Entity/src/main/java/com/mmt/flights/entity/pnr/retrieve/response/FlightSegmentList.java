package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FlightSegmentList {
    @JsonProperty("FlightSegment")
    private List<FlightSegment> FlightSegment;

    public List<FlightSegment> getFlightSegment() {
        return FlightSegment;
    }

    public void setFlightSegment(List<FlightSegment> flightSegment) {
        this.FlightSegment = flightSegment;
    }
}
