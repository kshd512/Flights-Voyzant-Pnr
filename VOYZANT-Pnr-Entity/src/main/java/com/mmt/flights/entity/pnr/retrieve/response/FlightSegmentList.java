package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlightSegmentList {
    @JsonProperty("FlightSegment")
    private List<FlightSegment> FlightSegment;

    public List<FlightSegment> getFlightSegment() {
        return FlightSegment;
    }

    public void setFlightSegment(List<FlightSegment> flightSegment) {
        this.FlightSegment = flightSegment;
    }
}
