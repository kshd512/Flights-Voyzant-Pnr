package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FareList {
    @JsonProperty("FareGroup")
    private List<FareGroup> fareGroup;

    public List<FareGroup> getFareGroup() {
        return fareGroup;
    }

    public void setFareGroup(List<FareGroup> fareGroup) {
        this.fareGroup = fareGroup;
    }
}
