package com.mmt.flights.entity.split.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Query {
    @JsonProperty("OrderID")
    private String orderId;
    
    @JsonProperty("GdsBookingReference")
    private String gdsBookingReference;
}