package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DataLists {
    @JsonProperty("PassengerList")
    private PassengerList passengerList;
    @JsonProperty("DisclosureList")
    private DisclosureList disclosureList;
    @JsonProperty("contactEmail")
    private List<String> contactEmail;
    @JsonProperty("contactNumber")
    private List<String> contactNumber;
    @JsonProperty("ContactAddress")
    private List<String> contactAddress;
    @JsonProperty("FareList")
    private FareList fareList;
    @JsonProperty("FlightSegmentList")
    private FlightSegmentList flightSegmentList;
    @JsonProperty("FlightList")
    private FlightList flightList;
    @JsonProperty("OriginDestinationList")
    private OriginDestinationList originDestinationList;
    @JsonProperty("PriceClassList")
    private PriceClassList priceClassList;
    @JsonProperty("BaggageAllowanceList")
    private BaggageAllowanceList baggageAllowanceList;
    @JsonProperty("FopList")
    private List<FopList> fopList;

    public PassengerList getPassengerList() {
        return passengerList;
    }

    public void setPassengerList(PassengerList passengerList) {
        this.passengerList = passengerList;
    }

    public DisclosureList getDisclosureList() {
        return disclosureList;
    }

    public void setDisclosureList(DisclosureList disclosureList) {
        this.disclosureList = disclosureList;
    }

    public List<String> getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(List<String> contactEmail) {
        this.contactEmail = contactEmail;
    }

    public List<String> getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(List<String> contactNumber) {
        this.contactNumber = contactNumber;
    }

    public List<String> getContactAddress() {
        return contactAddress;
    }

    public void setContactAddress(List<String> contactAddress) {
        this.contactAddress = contactAddress;
    }

    public FareList getFareList() {
        return fareList;
    }

    public void setFareList(FareList fareList) {
        this.fareList = fareList;
    }

    public FlightSegmentList getFlightSegmentList() {
        return flightSegmentList;
    }

    public void setFlightSegmentList(FlightSegmentList flightSegmentList) {
        this.flightSegmentList = flightSegmentList;
    }

    public FlightList getFlightList() {
        return flightList;
    }

    public void setFlightList(FlightList flightList) {
        this.flightList = flightList;
    }

    public OriginDestinationList getOriginDestinationList() {
        return originDestinationList;
    }

    public void setOriginDestinationList(OriginDestinationList originDestinationList) {
        this.originDestinationList = originDestinationList;
    }

    public PriceClassList getPriceClassList() {
        return priceClassList;
    }

    public void setPriceClassList(PriceClassList priceClassList) {
        this.priceClassList = priceClassList;
    }

    public BaggageAllowanceList getBaggageAllowanceList() {
        return baggageAllowanceList;
    }

    public void setBaggageAllowanceList(BaggageAllowanceList baggageAllowanceList) {
        this.baggageAllowanceList = baggageAllowanceList;
    }

    public List<FopList> getFopList() {
        return fopList;
    }

    public void setFopList(List<FopList> fopList) {
        this.fopList = fopList;
    }
}