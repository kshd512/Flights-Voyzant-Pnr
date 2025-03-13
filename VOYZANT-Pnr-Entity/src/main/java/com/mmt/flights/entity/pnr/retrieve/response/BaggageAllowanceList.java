package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaggageAllowanceList {
    @JsonProperty("BaggageAllowance")
    private List<BaggageAllowance> baggageAllowance;

    public List<BaggageAllowance> getBaggageAllowance() {
        return baggageAllowance;
    }

    public void setBaggageAllowance(List<BaggageAllowance> baggageAllowance) {
        this.baggageAllowance = baggageAllowance;
    }
}