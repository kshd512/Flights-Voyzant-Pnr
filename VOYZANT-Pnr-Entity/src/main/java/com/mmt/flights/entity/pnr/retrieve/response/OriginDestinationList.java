package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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