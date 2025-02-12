package com.mmt.flights.entity.pnr.retrieve.response;

public class Service {
    private String serviceID;
    private String passengerRefs;
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