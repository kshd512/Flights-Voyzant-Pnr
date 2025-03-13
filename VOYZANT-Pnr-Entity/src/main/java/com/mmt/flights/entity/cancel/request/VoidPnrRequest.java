package com.mmt.flights.entity.cancel.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoidPnrRequest {
    @JsonProperty("AirTicketVoidRQ")
    private OrderCancelRQ airTicketVoidRQ;

    public OrderCancelRQ getAirTicketVoidRQ() {
        return airTicketVoidRQ;
    }

    public void setAirTicketVoidRQ(OrderCancelRQ airTicketVoidRQ) {
        this.airTicketVoidRQ = airTicketVoidRQ;
    }
}
