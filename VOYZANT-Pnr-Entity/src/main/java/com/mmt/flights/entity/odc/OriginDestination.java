package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OriginDestination {
    @JsonProperty("PreviousDeparture")
    private AirportInfo previousDeparture;

    @JsonProperty("PreviousArrival")
    private AirportInfo previousArrival;

    @JsonProperty("PreviousCabinType")
    private String previousCabinType;

    @JsonProperty("Departure")
    private AirportInfo departure;

    @JsonProperty("Arrival")
    private AirportInfo arrival;

    @JsonProperty("CabinType")
    private String cabinType;

    public AirportInfo getPreviousDeparture() {
        return previousDeparture;
    }

    public void setPreviousDeparture(AirportInfo previousDeparture) {
        this.previousDeparture = previousDeparture;
    }

    public AirportInfo getPreviousArrival() {
        return previousArrival;
    }

    public void setPreviousArrival(AirportInfo previousArrival) {
        this.previousArrival = previousArrival;
    }

    public String getPreviousCabinType() {
        return previousCabinType;
    }

    public void setPreviousCabinType(String previousCabinType) {
        this.previousCabinType = previousCabinType;
    }

    public AirportInfo getDeparture() {
        return departure;
    }

    public void setDeparture(AirportInfo departure) {
        this.departure = departure;
    }

    public AirportInfo getArrival() {
        return arrival;
    }

    public void setArrival(AirportInfo arrival) {
        this.arrival = arrival;
    }

    public String getCabinType() {
        return cabinType;
    }

    public void setCabinType(String cabinType) {
        this.cabinType = cabinType;
    }
}