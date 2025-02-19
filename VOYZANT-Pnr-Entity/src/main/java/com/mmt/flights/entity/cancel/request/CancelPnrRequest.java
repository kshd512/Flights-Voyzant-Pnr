package com.mmt.flights.entity.cancel.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CancelPnrRequest {
    @JsonProperty("OrderCancelRQ")
    private OrderCancelRQ orderCancelRQ;

    public OrderCancelRQ getOrderCancelRQ() {
        return orderCancelRQ;
    }

    public void setOrderCancelRQ(OrderCancelRQ orderCancelRQ) {
        this.orderCancelRQ = orderCancelRQ;
    }
}