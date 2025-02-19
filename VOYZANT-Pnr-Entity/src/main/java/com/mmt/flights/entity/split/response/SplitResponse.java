package com.mmt.flights.entity.split.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SplitResponse {
    @JsonProperty("Status")
    private String status;
    
    @JsonProperty("Msg")
    private String msg;
    
    @JsonProperty("OrderID")
    private String orderId;
    
    @JsonProperty("GdsBookingReference")
    private String gdsBookingReference;
    
    @JsonProperty("NewGdsBookingReference")
    private String newGdsBookingReference;
}