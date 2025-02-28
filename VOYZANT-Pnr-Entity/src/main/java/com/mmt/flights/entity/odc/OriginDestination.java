package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OriginDestination {
    @JsonProperty("PreviousDeparture")
    private Airport previousDeparture;

    @JsonProperty("PreviousArrival")
    private Airport previousArrival;

    @JsonProperty("PreviousCabinType")
    private String previousCabinType;

    @JsonProperty("Departure")
    private Airport departure;

    @JsonProperty("Arrival")
    private Airport arrival;

    @JsonProperty("CabinType")
    private String cabinType;

    public Airport getPreviousDeparture() {
        return previousDeparture;
    }

    public void setPreviousDeparture(Airport previousDeparture) {
        this.previousDeparture = previousDeparture;
    }

    public Airport getPreviousArrival() {
        return previousArrival;
    }

    public void setPreviousArrival(Airport previousArrival) {
        this.previousArrival = previousArrival;
    }

    public String getPreviousCabinType() {
        return previousCabinType;
    }

    public void setPreviousCabinType(String previousCabinType) {
        this.previousCabinType = previousCabinType;
    }

    public Airport getDeparture() {
        return departure;
    }

    public void setDeparture(Airport departure) {
        this.departure = departure;
    }

    public Airport getArrival() {
        return arrival;
    }

    public void setArrival(Airport arrival) {
        this.arrival = arrival;
    }

    public String getCabinType() {
        return cabinType;
    }

    public void setCabinType(String cabinType) {
        this.cabinType = cabinType;
    }
}