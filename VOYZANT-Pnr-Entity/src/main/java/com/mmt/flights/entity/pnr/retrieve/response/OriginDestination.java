package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OriginDestination {
    @JsonProperty("OriginDestinationKey")
    private String originDestinationKey;
    
    @JsonProperty("DepartureCode")
    private String departureCode;
    
    @JsonProperty("ArrivalCode")
    private String arrivalCode;
    
    @JsonProperty("FlightReferences")
    private String flightReferences;

    public String getOriginDestinationKey() {
        return originDestinationKey;
    }

    public void setOriginDestinationKey(String originDestinationKey) {
        this.originDestinationKey = originDestinationKey;
    }

    public String getDepartureCode() {
        return departureCode;
    }

    public void setDepartureCode(String departureCode) {
        this.departureCode = departureCode;
    }

    public String getArrivalCode() {
        return arrivalCode;
    }

    public void setArrivalCode(String arrivalCode) {
        this.arrivalCode = arrivalCode;
    }

    public String getFlightReferences() {
        return flightReferences;
    }

    public void setFlightReferences(String flightReferences) {
        this.flightReferences = flightReferences;
    }
}