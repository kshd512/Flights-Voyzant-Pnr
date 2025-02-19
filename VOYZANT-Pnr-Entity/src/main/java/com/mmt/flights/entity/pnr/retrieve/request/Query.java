package com.mmt.flights.entity.pnr.retrieve.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Query {
    @JsonProperty("OrderID")
    private String orderId;
    
    @JsonProperty("GdsBookingReference")
    private List<String> gdsBookingReference;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<String> getGdsBookingReference() {
        return gdsBookingReference;
    }

    public void setGdsBookingReference(List<String> gdsBookingReference) {
        this.gdsBookingReference = gdsBookingReference;
    }
}