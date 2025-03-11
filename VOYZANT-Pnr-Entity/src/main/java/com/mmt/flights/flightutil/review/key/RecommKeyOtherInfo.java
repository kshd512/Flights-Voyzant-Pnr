package com.mmt.flights.flightutil.review.key;

public class RecommKeyOtherInfo {
    private String flightServiceName;
    private int pnrGroup;
    private int rtFareFlag;

    public void setFlightServiceName(String flightServiceName) {
        this.flightServiceName = flightServiceName;
    }

    public void setPnrGroup(int pnrGroup) {
        this.pnrGroup = pnrGroup;
    }

    public void setRtFareFlag(int rtFareFlag) {
        this.rtFareFlag = rtFareFlag;
    }

    public String build() {
        return String.format("|%s|%d|%d", flightServiceName, pnrGroup, rtFareFlag);
    }
}