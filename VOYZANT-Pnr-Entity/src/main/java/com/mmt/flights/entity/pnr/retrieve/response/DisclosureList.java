package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DisclosureList {
    @JsonProperty("Disclosures")
    private List<Object> disclosures;

    public List<Object> getDisclosures() {
        return disclosures;
    }

    public void setDisclosures(List<Object> disclosures) {
        this.disclosures = disclosures;
    }
}