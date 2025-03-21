package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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