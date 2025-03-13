package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlightQuery {
    @JsonProperty("OriginDestinations")
    private OriginDestinations originDestinations;

    public OriginDestinations getOriginDestinations() {
        return originDestinations;
    }

    public void setOriginDestinations(OriginDestinations originDestinations) {
        this.originDestinations = originDestinations;
    }
}