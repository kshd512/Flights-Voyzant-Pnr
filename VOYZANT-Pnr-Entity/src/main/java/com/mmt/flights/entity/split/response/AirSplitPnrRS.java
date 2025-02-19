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
    private String originalOrderID;
    
    @JsonProperty("SplitedOrderID")
    private String splitedOrderID;
    
    @JsonProperty("OriginalGdsBookingReference")
    private String originalGdsBookingReference;
    
    @JsonProperty("SplitedGdsBookingReference")
    private String splitedGdsBookingReference;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}