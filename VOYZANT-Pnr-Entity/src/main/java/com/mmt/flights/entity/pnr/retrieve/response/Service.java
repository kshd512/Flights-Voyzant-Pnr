package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Service {
    @JsonProperty("ServiceID")
    private String serviceID;
    @JsonProperty("PassengerRefs")
    private String passengerRefs;
    @JsonProperty("FlightRefs")
    private String flightRefs;

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getPassengerRefs() {
        return passengerRefs;
    }

    public void setPassengerRefs(String passengerRefs) {
        this.passengerRefs = passengerRefs;
    }

    public String getFlightRefs() {
        return flightRefs;
    }

    public void setFlightRefs(String flightRefs) {
        this.flightRefs = flightRefs;
    }
}