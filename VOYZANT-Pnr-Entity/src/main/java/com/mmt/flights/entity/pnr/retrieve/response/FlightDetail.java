package com.mmt.flights.entity.pnr.retrieve.response;

import java.util.List;

public class FlightDetail {
    private Duration flightDuration;
    private Stops stops;
    private List<Object> interMediate;
    private String airMilesFlown;

    public Duration getFlightDuration() {
        return flightDuration;
    }

    public void setFlightDuration(Duration flightDuration) {
        this.flightDuration = flightDuration;
    }

    public Stops getStops() {
        return stops;
    }

    public void setStops(Stops stops) {
        this.stops = stops;
    }

    public List<Object> getInterMediate() {
        return interMediate;
    }

    public void setInterMediate(List<Object> interMediate) {
        this.interMediate = interMediate;
    }

    public String getAirMilesFlown() {
        return airMilesFlown;
    }

    public void setAirMilesFlown(String airMilesFlown) {
        this.airMilesFlown = airMilesFlown;
    }
}