package com.mmt.flights.entity.cancel.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CancelPnrResponse {
    @JsonProperty("OrderViewRS")
    private OrderViewRS orderViewRS;

    public OrderViewRS getOrderViewRS() {
        return orderViewRS;
    }

    public void setOrderViewRS(OrderViewRS orderViewRS) {
        this.orderViewRS = orderViewRS;
    }
}