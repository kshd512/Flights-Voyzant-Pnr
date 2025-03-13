package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDocInfos {
    @JsonProperty("TicketDocInfo")
    private List<TicketDocInfo> ticketDocInfo;

    public List<TicketDocInfo> getTicketDocInfo() {
        return ticketDocInfo;
    }

    public void setTicketDocInfo(List<TicketDocInfo> ticketDocInfo) {
        this.ticketDocInfo = ticketDocInfo;
    }
}