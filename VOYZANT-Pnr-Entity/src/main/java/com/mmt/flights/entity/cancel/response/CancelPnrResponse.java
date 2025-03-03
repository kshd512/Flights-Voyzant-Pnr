package com.mmt.flights.entity.cancel.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
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