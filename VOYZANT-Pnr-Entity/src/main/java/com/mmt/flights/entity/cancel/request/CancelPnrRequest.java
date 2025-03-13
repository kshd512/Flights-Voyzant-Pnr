package com.mmt.flights.entity.cancel.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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