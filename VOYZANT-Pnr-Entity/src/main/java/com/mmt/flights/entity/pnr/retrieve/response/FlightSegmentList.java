package com.mmt.flights.entity.pnr.retrieve.response;

import java.util.List;

public class FlightSegmentList {
    private List<FlightSegment> flightSegment;

    public List<FlightSegment> getFlightSegment() {
        return flightSegment;
    }

    public void setFlightSegment(List<FlightSegment> flightSegment) {
        this.flightSegment = flightSegment;
    }
}