package com.mmt.flights.entity.pnr.retrieve.response;

import java.util.List;

public class PassengerList {
    private List<Passenger> passengers;

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }
}