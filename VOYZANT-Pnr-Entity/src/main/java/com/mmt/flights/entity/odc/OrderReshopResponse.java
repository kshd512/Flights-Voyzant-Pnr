package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderReshopResponse {
    @JsonProperty("OrderReshopRS")
    private OrderReshopRS orderReshopRS;

    public OrderReshopRS getOrderReshopRS() {
        return orderReshopRS;
    }

    public void setOrderReshopRS(OrderReshopRS orderReshopRS) {
        this.orderReshopRS = orderReshopRS;
    }
}