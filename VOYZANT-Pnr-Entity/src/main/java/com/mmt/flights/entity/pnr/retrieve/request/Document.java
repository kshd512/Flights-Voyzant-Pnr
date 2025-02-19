package com.mmt.flights.entity.pnr.retrieve.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Document {
    @JsonProperty("Name")
    private String name;
    
    @JsonProperty("referenceversion")
    private String referenceversion;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReferenceversion() {
        return referenceversion;
    }

    public void setReferenceversion(String referenceversion) {
        this.referenceversion = referenceversion;
    }
}
