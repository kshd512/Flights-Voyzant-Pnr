package com.mmt.flights.entity.cancel.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Response {
    @JsonProperty("Status")
    private String status;
    
    @JsonProperty("Msg")
    private String msg;
    
    @JsonProperty("BookingStatus")
    private String bookingStatus;
    
    @JsonProperty("OrderID")
    private String orderID;
    
    @JsonProperty("GdsBookingReference")
    private String gdsBookingReference;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getGdsBookingReference() {
        return gdsBookingReference;
    }

    public void setGdsBookingReference(String gdsBookingReference) {
        this.gdsBookingReference = gdsBookingReference;
    }
}