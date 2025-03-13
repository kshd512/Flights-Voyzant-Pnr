package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AirportInfo {
    @JsonProperty("AirportCode")
    private String AirportCode;
    
    @JsonProperty("Date")
    private String Date;
    
    @JsonProperty("Time")
    private String Time;
    
    @JsonProperty("AirportName")
    private String AirportName;
    
    @JsonProperty("Terminal")
    private Terminal Terminal;

    public String getAirportCode() {
        return AirportCode;
    }
    public void setAirportCode(String airportCode) {
        AirportCode = airportCode;
    }

    public String getDate() {
        return Date;
    }
    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }
    public void setTime(String time) {
        Time = time;
    }

    public String getAirportName() {
        return AirportName;
    }
    public void setAirportName(String airportName) {
        AirportName = airportName;
    }

    public Terminal getTerminal() {
        return Terminal;
    }
    public void setTerminal(Terminal terminal) {
        Terminal = terminal;
    }
}
