package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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