package com.mmt.flights.entity.cancel.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Query {
    @JsonProperty("OrderID")
    private String orderID;
    
    @JsonProperty("GdsBookingReference")
    private String[] gdsBookingReference;

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String[] getGdsBookingReference() {
        return gdsBookingReference;
    }

    public void setGdsBookingReference(String[] gdsBookingReference) {
        this.gdsBookingReference = gdsBookingReference;
    }
}