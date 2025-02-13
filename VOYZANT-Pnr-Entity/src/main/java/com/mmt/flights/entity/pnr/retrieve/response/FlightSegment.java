package com.mmt.flights.entity.pnr.retrieve.response;

public class FlightSegment {
    private String SegmentKey;
    private AirportInfo Departure;
    private AirportInfo Arrival;
    private Carrier MarketingCarrier;
    private Carrier OperatingCarrier;
    private Equipment Equipment;
    private Code Code;
    private FlightDetail FlightDetail;
    private String BrandId;

    public String getSegmentKey() {
        return SegmentKey;
    }
    public void setSegmentKey(String segmentKey) {
        SegmentKey = segmentKey;
    }

    public AirportInfo getDeparture() {
        return Departure;
    }
    public void setDeparture(AirportInfo departure) {
        Departure = departure;
    }

    public AirportInfo getArrival() {
        return Arrival;
    }
    public void setArrival(AirportInfo arrival) {
        Arrival = arrival;
    }

    public Carrier getMarketingCarrier() {
        return MarketingCarrier;
    }
    public void setMarketingCarrier(Carrier marketingCarrier) {
        MarketingCarrier = marketingCarrier;
    }

    public Carrier getOperatingCarrier() {
        return OperatingCarrier;
    }
    public void setOperatingCarrier(Carrier operatingCarrier) {
        OperatingCarrier = operatingCarrier;
    }

    public Equipment getEquipment() {
        return Equipment;
    }
    public void setEquipment(Equipment equipment) {
        Equipment = equipment;
    }

    public Code getCode() {
        return Code;
    }
    public void setCode(Code code) {
        this.Code = code;
    }

    public FlightDetail getFlightDetail() {
        return FlightDetail;
    }
    public void setFlightDetail(FlightDetail flightDetail) {
        FlightDetail = flightDetail;
    }

    public String getBrandId() {
        return BrandId;
    }
    public void setBrandId(String brandId) {
        BrandId = brandId;
    }
}
