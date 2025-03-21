package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mmt.flights.entity.pnr.retrieve.response.*;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataLists {
    @JsonProperty("PassengerList")
    private PassengerList passengerList;

    @JsonProperty("DisclosureList")
    private DisclosureList disclosureList;

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

    @JsonProperty("ContactList")
    private ContactList contactList;

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

    public ContactList getContactList() {
        return contactList;
    }

    public void setContactList(ContactList contactList) {
        this.contactList = contactList;
    }
}