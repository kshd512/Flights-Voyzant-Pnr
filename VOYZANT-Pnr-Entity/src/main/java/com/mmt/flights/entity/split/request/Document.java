package com.mmt.flights.entity.split.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Document {
    @JsonProperty("Name")
    private String name;
    
    @JsonProperty("ReferenceVersion")
    private String referenceVersion;
}