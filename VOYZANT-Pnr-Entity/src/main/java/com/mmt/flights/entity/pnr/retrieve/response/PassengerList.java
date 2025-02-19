package com.mmt.flights.entity.pnr.retrieve.response;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PassengerList {
    @JsonProperty("Passengers")
    private List<Passenger> passengers;

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }
}