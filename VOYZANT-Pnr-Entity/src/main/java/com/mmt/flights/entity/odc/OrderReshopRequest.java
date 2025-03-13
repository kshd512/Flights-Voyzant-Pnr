package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderReshopRequest {
    @JsonProperty("OrderReshopRQ")
    private OrderReshopRQ orderReshopRQ;
    
    @JsonProperty("OrderChangeRQ")
    private OrderReshopRQ orderChangeRQ;

    public OrderReshopRQ getOrderReshopRQ() {
        return orderReshopRQ;
    }

    public void setOrderReshopRQ(OrderReshopRQ orderReshopRQ) {
        this.orderReshopRQ = orderReshopRQ;
    }
    
    public OrderReshopRQ getOrderChangeRQ() {
        return orderChangeRQ;
    }

    public void setOrderChangeRQ(OrderReshopRQ orderChangeRQ) {
        this.orderChangeRQ = orderChangeRQ;
    }
}