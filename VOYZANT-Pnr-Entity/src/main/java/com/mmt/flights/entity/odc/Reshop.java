package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Reshop {
    @JsonProperty("OrderServicing")
    private OrderServicing orderServicing;

    public OrderServicing getOrderServicing() {
        return orderServicing;
    }

    public void setOrderServicing(OrderServicing orderServicing) {
        this.orderServicing = orderServicing;
    }
}