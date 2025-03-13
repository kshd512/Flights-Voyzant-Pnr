package com.mmt.flights.entity.pnr.retrieve.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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