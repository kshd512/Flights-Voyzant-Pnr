package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Add {
    @JsonProperty("FlightQuery")
    private FlightQuery flightQuery;
    
    @JsonProperty("Qualifier")
    private Qualifier qualifier;

    public FlightQuery getFlightQuery() {
        return flightQuery;
    }

    public void setFlightQuery(FlightQuery flightQuery) {
        this.flightQuery = flightQuery;
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    public void setQualifier(Qualifier qualifier) {
        this.qualifier = qualifier;
    }
}