package com.mmt.flights.entity.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mmt.flights.entity.odc.OrderServicing;
import com.mmt.flights.entity.odc.Reshop;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Query {
    @JsonProperty("OrderID")
    private String orderId;
    
    @JsonProperty("GdsBookingReference")
    private String[] gdsBookingReference;

    @JsonProperty("TicketNumber")
    private String[] ticketNumber;

    @JsonProperty("NeedToCancelBooking")
    private String needToCancelBooking;

    @JsonProperty("Reshop")
    private Reshop reshop;

    @JsonProperty("OrderServicing")
    private OrderServicing orderServicing;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public Reshop getReshop() {
        return reshop;
    }

    public void setReshop(Reshop reshop) {
        this.reshop = reshop;
    }

    public OrderServicing getOrderServicing() {
        return orderServicing;
    }

    public void setOrderServicing(OrderServicing orderServicing) {
        this.orderServicing = orderServicing;
    }
}