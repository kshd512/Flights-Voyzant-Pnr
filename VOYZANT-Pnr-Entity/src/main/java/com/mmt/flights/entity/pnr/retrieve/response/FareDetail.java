package com.mmt.flights.entity.pnr.retrieve.response;

public class FareDetail {
    private String passengerRefs;
    private Price price;

    public String getPassengerRefs() {
        return passengerRefs;
    }

    public void setPassengerRefs(String passengerRefs) {
        this.passengerRefs = passengerRefs;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }
}