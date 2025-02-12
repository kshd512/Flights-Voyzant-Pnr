package com.mmt.flights.entity.pnr.retrieve.response;

public class FlightSegment {
    private String segmentKey;
    private Airport departure;
    private Airport arrival;
    private Carrier marketingCarrier;
    private Carrier operatingCarrier;
    private Equipment equipment;
    private Code code;
    private FlightDetail flightDetail;
    private String brandId;

    public String getSegmentKey() {
        return segmentKey;
    }

    public void setSegmentKey(String segmentKey) {
        this.segmentKey = segmentKey;
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

    public Carrier getMarketingCarrier() {
        return marketingCarrier;
    }

    public void setMarketingCarrier(Carrier marketingCarrier) {
        this.marketingCarrier = marketingCarrier;
    }

    public Carrier getOperatingCarrier() {
        return operatingCarrier;
    }

    public void setOperatingCarrier(Carrier operatingCarrier) {
        this.operatingCarrier = operatingCarrier;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public FlightDetail getFlightDetail() {
        return flightDetail;
    }

    public void setFlightDetail(FlightDetail flightDetail) {
        this.flightDetail = flightDetail;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }
}