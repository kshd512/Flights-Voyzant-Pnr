package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlightDetail {
    @JsonProperty("FlightDuration")
    private Duration flightDuration;
    @JsonProperty("Stops")
    private Stops stops;
    @JsonProperty("InterMediate")
    private List<Object> interMediate;
    @JsonProperty("AirMilesFlown")
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