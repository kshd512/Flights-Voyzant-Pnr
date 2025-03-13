package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderChangeResponse {
    @JsonProperty("OrderViewRS")
    private OrderViewRS orderViewRS;

    public OrderViewRS getOrderViewRS() {
        return orderViewRS;
    }

    public void setOrderViewRS(OrderViewRS orderViewRS) {
        this.orderViewRS = orderViewRS;
    }
}
