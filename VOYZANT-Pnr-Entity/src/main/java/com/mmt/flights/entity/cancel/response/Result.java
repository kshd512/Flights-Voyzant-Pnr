package com.mmt.flights.entity.cancel.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {

    @JsonProperty("Status")
    private String status;

    @JsonProperty("ErrorMessage")
    private String errorMessage;

    @JsonProperty("TicketDetails")
    private List<TicketDetail> ticketDetails;

    @JsonProperty("TktRequestId")
    private String tktRequestId;

    @JsonProperty("ShoppingResponseId")
    private String shoppingResponseId;

    @JsonProperty("BookingStatus")
    private String bookingStatus;

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<TicketDetail> getTicketDetails() {
        return ticketDetails;
    }

    public void setTicketDetails(List<TicketDetail> ticketDetails) {
        this.ticketDetails = ticketDetails;
    }

    public String getTktRequestId() {
        return tktRequestId;
    }

    public void setTktRequestId(String tktRequestId) {
        this.tktRequestId = tktRequestId;
    }

    public String getShoppingResponseId() {
        return shoppingResponseId;
    }

    public void setShoppingResponseId(String shoppingResponseId) {
        this.shoppingResponseId = shoppingResponseId;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}