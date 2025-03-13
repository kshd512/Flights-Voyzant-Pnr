package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDocument {
    @JsonProperty("Type")
    private String type;
    
    @JsonProperty("TicketDocNbr")
    private String ticketDocNbr;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTicketDocNbr() {
        return ticketDocNbr;
    }

    public void setTicketDocNbr(String ticketDocNbr) {
        this.ticketDocNbr = ticketDocNbr;
    }
}