package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Airport {
    @JsonProperty("AirportCode")
    private String airportCode;

    @JsonProperty("Date")
    private String date;

    public Airport(String airportCode, String date) {
        this.airportCode = airportCode;
        this.date = date;
    }

    public Airport() {
        // Default constructor for Jackson
    }

    public String getAirportCode() {
        return airportCode;
    }

    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}