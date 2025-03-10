package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketDocInfo {
    @JsonProperty("TicketDocument")
    private TicketDocument ticketDocument;
    
    @JsonProperty("PassengerReference")
    private String passengerReference;
    
    @JsonProperty("GdsBookingReference")
    private String gdsBookingReference;

    public TicketDocument getTicketDocument() {
        return ticketDocument;
    }

    public void setTicketDocument(TicketDocument ticketDocument) {
        this.ticketDocument = ticketDocument;
    }

    public String getPassengerReference() {
        return passengerReference;
    }

    public void setPassengerReference(String passengerReference) {
        this.passengerReference = passengerReference;
    }

    public String getGdsBookingReference() {
        return gdsBookingReference;
    }

    public void setGdsBookingReference(String gdsBookingReference) {
        this.gdsBookingReference = gdsBookingReference;
    }
}