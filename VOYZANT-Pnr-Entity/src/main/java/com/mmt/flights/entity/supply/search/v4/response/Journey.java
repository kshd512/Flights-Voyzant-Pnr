
package com.mmt.flights.entity.supply.search.v4.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public class Journey {

    @JsonProperty("flightType")
    private Integer flightType;
    @JsonProperty("stops")
    private Integer stops;
    @JsonProperty("designator")
    @Valid
    private Designator designator;
    @JsonProperty("segments")
    @Valid
    private List<Segment> segments = null;
    @JsonProperty("journeyKey")
    private String journeyKey;

    @JsonProperty("fares")
    private List<FareReference> fares = null;

    @JsonProperty("flightType")
    public Integer getFlightType() {
        return flightType;
    }

    @JsonProperty("flightType")
    public void setFlightType(Integer flightType) {
        this.flightType = flightType;
    }

    @JsonProperty("stops")
    public Integer getStops() {
        return stops;
    }

    @JsonProperty("stops")
    public void setStops(Integer stops) {
        this.stops = stops;
    }

    @JsonProperty("designator")
    public Designator getDesignator() {
        return designator;
    }

    @JsonProperty("designator")
    public void setDesignator(Designator designator) {
        this.designator = designator;
    }

    @JsonProperty("segments")
    public List<Segment> getSegments() {
        return segments;
    }

    @JsonProperty("segments")
    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }

    @JsonProperty("journeyKey")
    public String getJourneyKey() {
        return journeyKey;
    }

    @JsonProperty("journeyKey")
    public void setJourneyKey(String journeyKey) {
        this.journeyKey = journeyKey;
    }

    @JsonProperty("fares")
    public List<FareReference> getFares() {
        return fares;
    }

    @JsonProperty("fares")
    public void setFares(List<FareReference> fares) {
        this.fares = fares;
    }
}

