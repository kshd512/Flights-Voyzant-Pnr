package com.mmt.flights.entity.pnr.retrieve.request;

import java.util.List;

class Query {
    private String OrderID;
    private List<String> GdsBookingReference;

    // Getters and Setters
    public String getOrderID() {
        return OrderID;
    }

    public void setOrderID(String orderID) {
        this.OrderID = orderID;
    }

    public List<String> getGdsBookingReference() {
        return GdsBookingReference;
    }

    public void setGdsBookingReference(List<String> gdsBookingReference) {
        this.GdsBookingReference = gdsBookingReference;
    }
}