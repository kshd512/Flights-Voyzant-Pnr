package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AirportInfo {
    @JsonProperty("AirportCode")
    private String airportCode;

    @JsonProperty("Date")
    private String date;

    public AirportInfo(String airportCode, String date) {
        this.airportCode = airportCode;
        this.date = date;
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