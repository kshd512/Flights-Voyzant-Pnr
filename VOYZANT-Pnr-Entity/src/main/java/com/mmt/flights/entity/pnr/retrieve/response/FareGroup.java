package com.mmt.flights.entity.pnr.retrieve.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
class FareGroup {
    @JsonProperty("FareGroupRef")
    private String fareGroupRef;

    @JsonProperty("FareCode")
    private String fareCode;

    @JsonProperty("FareBasisCode")
    private String fareBasisCode;

    public String getFareGroupRef() {
        return fareGroupRef;
    }

    public void setFareGroupRef(String fareGroupRef) {
        this.fareGroupRef = fareGroupRef;
    }

    public String getFareCode() {
        return fareCode;
    }

    public void setFareCode(String fareCode) {
        this.fareCode = fareCode;
    }

    public String getFareBasisCode() {
        return fareBasisCode;
    }

    public void setFareBasisCode(String fareBasisCode) {
        this.fareBasisCode = fareBasisCode;
    }
}