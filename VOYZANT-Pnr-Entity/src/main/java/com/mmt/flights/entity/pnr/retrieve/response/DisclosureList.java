package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

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