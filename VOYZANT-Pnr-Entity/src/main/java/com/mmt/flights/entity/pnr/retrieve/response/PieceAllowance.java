package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PieceAllowance {
    @JsonProperty("ApplicableParty")
    private String applicableParty;
    
    @JsonProperty("TotalQuantity")
    private String totalQuantity;
    
    @JsonProperty("Unit")
    private String unit;

    public String getApplicableParty() {
        return applicableParty;
    }

    public void setApplicableParty(String applicableParty) {
        this.applicableParty = applicableParty;
    }

    public String getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(String totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}