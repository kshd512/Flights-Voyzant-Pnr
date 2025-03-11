package com.mmt.flights.flightutil.review.key;

public class RecommKeySegment {
    public enum StopType {
        PLANE_CHANGE,
        JOURNEY_OVER
    }

    private String depCityCode;
    private String depDate;
    private String depTime;
    private String depTerminal;
    private String arrCityCode;
    private String arrDate;
    private String arrTime;
    private String arrTerminal;
    private String flightNumber;
    private String marketingAirline;
    private String operatingAirline;
    private String validatingAirline;
    private StopType stopType;
    private String cmsId;
    private String flightServiceName;

    public void setDepCityCode(String depCityCode) {
        this.depCityCode = depCityCode;
    }

    public void setDepDate(String depDate) {
        this.depDate = depDate;
    }

    public void setDepTime(String depTime) {
        this.depTime = depTime;
    }

    public void setDepTerminal(String depTerminal) {
        this.depTerminal = depTerminal;
    }

    public void setArrCityCode(String arrCityCode) {
        this.arrCityCode = arrCityCode;
    }

    public void setArrDate(String arrDate) {
        this.arrDate = arrDate;
    }

    public void setArrTime(String arrTime) {
        this.arrTime = arrTime;
    }

    public void setArrTerminal(String arrTerminal) {
        this.arrTerminal = arrTerminal;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public void setMarketingAirline(String marketingAirline) {
        this.marketingAirline = marketingAirline;
    }

    public void setOperatingAirline(String operatingAirline) {
        this.operatingAirline = operatingAirline;
    }

    public void setValidatingAirline(String validatingAirline) {
        this.validatingAirline = validatingAirline;
    }

    public void setStopType(StopType stopType) {
        this.stopType = stopType;
    }

    public void setCmsId(String cmsId) {
        this.cmsId = cmsId;
    }

    public void setFlightServiceName(String flightServiceName) {
        this.flightServiceName = flightServiceName;
    }

    public String build() {
        return String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s",
            depCityCode, depDate, depTime, depTerminal != null ? depTerminal : "",
            arrCityCode, arrDate, arrTime, arrTerminal != null ? arrTerminal : "",
            flightNumber, marketingAirline, operatingAirline, validatingAirline,
            stopType == StopType.JOURNEY_OVER ? "JO" : "PC",
            cmsId, flightServiceName);
    }
}