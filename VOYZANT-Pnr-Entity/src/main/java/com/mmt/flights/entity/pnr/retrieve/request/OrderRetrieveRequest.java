package com.mmt.flights.entity.pnr.retrieve.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderRetrieveRequest {
    @JsonProperty("OrderRetreiveRQ")
    private OrderRetreiveRQ orderRetreiveRQ;

    public OrderRetreiveRQ getOrderRetreiveRQ() {
        return orderRetreiveRQ;
    }

    public void setOrderRetreiveRQ(OrderRetreiveRQ orderRetreiveRQ) {
        this.orderRetreiveRQ = orderRetreiveRQ;
    }
}