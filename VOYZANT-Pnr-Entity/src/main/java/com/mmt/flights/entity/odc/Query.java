package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Query {
    @JsonProperty("OrderID")
    private String orderID;

    @JsonProperty("GDSBookingReference")
    private List<String> gdsBookingReference = new ArrayList<>();

    @JsonProperty("Reshop")
    private Reshop reshop;

    @JsonProperty("OrderServicing")
    private OrderServicing orderServicing;

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public List<String> getGdsBookingReference() {
        return gdsBookingReference;
    }

    public void setGdsBookingReference(List<String> gdsBookingReference) {
        this.gdsBookingReference = gdsBookingReference;
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