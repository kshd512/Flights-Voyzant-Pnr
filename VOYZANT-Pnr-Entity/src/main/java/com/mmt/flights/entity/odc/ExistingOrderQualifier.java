package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExistingOrderQualifier {
    @JsonProperty("OrderKeys")
    private OrderKeys orderKeys;

    public OrderKeys getOrderKeys() {
        return orderKeys;
    }

    public void setOrderKeys(OrderKeys orderKeys) {
        this.orderKeys = orderKeys;
    }
}