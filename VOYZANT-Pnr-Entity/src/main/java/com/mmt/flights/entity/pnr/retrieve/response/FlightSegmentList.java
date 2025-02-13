package com.mmt.flights.entity.pnr.retrieve.response;


import java.util.List;

public class FlightSegmentList {
    private List<FlightSegment> FlightSegment;

    public List<FlightSegment> getFlightSegment() {
        return FlightSegment;
    }

    public void setFlightSegment(List<FlightSegment> flightSegment) {
        this.FlightSegment = flightSegment;
    }
}
