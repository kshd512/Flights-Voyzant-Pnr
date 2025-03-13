package com.mmt.flights.entity.cancel.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoidPnrResponse {

    @JsonProperty("AirTicketVoidRS")
    private TicketVoidRS ticketVoidRS;

    public TicketVoidRS getTicketVoidRS() {
        return ticketVoidRS;
    }

    public void setTicketVoidRS(TicketVoidRS ticketVoidRS) {
        this.ticketVoidRS = ticketVoidRS;
    }
}
