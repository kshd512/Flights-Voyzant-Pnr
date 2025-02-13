package com.mmt.flights.entity.pnr.retrieve.response;

public class AirportInfo {
    private String AirportCode;
    private String Date;
    private String Time;
    private String AirportName;
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
