package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OriginDestinationList {
    @JsonProperty("OriginDestination")
    private List<OriginDestination> originDestination;

    public List<OriginDestination> getOriginDestination() {
        return originDestination;
    }

    public void setOriginDestination(List<OriginDestination> originDestination) {
        this.originDestination = originDestination;
    }
}