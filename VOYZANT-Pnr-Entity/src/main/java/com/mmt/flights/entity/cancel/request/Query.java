package com.mmt.flights.entity.cancel.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Query {
    @JsonProperty("OrderID")
    private String orderID;
    
    @JsonProperty("GdsBookingReference")
    private String[] gdsBookingReference;

    @JsonProperty("TicketNumber")
    private String[] ticketNumber;

    @JsonProperty("NeedToCancelBooking")
    private String needToCancelBooking;

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

    public String[] getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String[] ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getNeedToCancelBooking() {
        return needToCancelBooking;
    }

    public void setNeedToCancelBooking(String needToCancelBooking) {
        this.needToCancelBooking = needToCancelBooking;
    }
}