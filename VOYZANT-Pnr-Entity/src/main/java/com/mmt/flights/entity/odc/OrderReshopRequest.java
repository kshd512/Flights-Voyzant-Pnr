package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderReshopRequest {
    @JsonProperty("OrderReshopRQ")
    private OrderReshopRQ orderReshopRQ;

    public OrderReshopRQ getOrderReshopRQ() {
        return orderReshopRQ;
    }

    public void setOrderReshopRQ(OrderReshopRQ orderReshopRQ) {
        this.orderReshopRQ = orderReshopRQ;
    }
}