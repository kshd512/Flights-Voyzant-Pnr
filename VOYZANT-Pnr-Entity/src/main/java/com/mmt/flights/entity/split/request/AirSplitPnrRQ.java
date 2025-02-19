package com.mmt.flights.entity.split.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AirSplitPnrRQ {
    @JsonProperty("Document")
    private Document document;
    
    @JsonProperty("Party")
    private Party party;
    
    @JsonProperty("Query")
    private Query query;
    
    @JsonProperty("DataLists")
    private DataLists dataLists;
}