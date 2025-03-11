
package com.mmt.flights.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "destination",
    "origin",
    "arrival",
    "departure"
})
public class Designator {

    @JsonProperty("destination")
    private String destination;
    @JsonProperty("origin")
    private String origin;
    @JsonProperty("arrival")
    private String arrival;
    @JsonProperty("departure")
    private String departure;

    @JsonProperty("destination")
    public String getDestination() {
        return destination;
    }

    @JsonProperty("destination")
    public void setDestination(String destination) {
        this.destination = destination;
    }

    @JsonProperty("origin")
    public String getOrigin() {
        return origin;
    }

    @JsonProperty("origin")
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @JsonProperty("arrival")
    public String getArrival() {
        return arrival;
    }

    @JsonProperty("arrival")
    public void setArrival(String arrival) {
        this.arrival = arrival;
    }

    @JsonProperty("departure")
    public String getDeparture() {
        return departure;
    }

    @JsonProperty("departure")
    public void setDeparture(String departure) {
        this.departure = departure;
    }

}
