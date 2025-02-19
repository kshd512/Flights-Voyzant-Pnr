package com.mmt.flights.entity.split.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Document {
    @JsonProperty("Name")
    private String name;
    
    @JsonProperty("ReferenceVersion")
    private String referenceVersion;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}