package com.mmt.flights.entity.split.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AirSplitPnrRS {
    @JsonProperty("Document")
    private Document document;
    
    @JsonProperty("Party")
    private Party party;
    
    @JsonProperty("ShoppingResponseId")
    private String shoppingResponseId;
    
    @JsonProperty("Success")
    private Success success;
    
    @JsonProperty("OriginalOrderID")
    private String originalOrderId;
    
    @JsonProperty("SplitedOrderID")
    private String splitedOrderId;
    
    @JsonProperty("OriginalGdsBookingReference")
    private String originalGdsBookingReference;
    
    @JsonProperty("SplitedGdsBookingReference")
    private String splitedGdsBookingReference;
}