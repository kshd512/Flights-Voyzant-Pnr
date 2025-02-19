package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TravelAgencySender {
    @JsonProperty("Name")
    private String name;
    @JsonProperty("IATA_Number")
    private String iataNumber;
    @JsonProperty("AgencyID")
    private String agencyID;
    @JsonProperty("Contacts")
    private Contacts contacts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIataNumber() {
        return iataNumber;
    }

    public void setIataNumber(String iataNumber) {
        this.iataNumber = iataNumber;
    }

    public String getAgencyID() {
        return agencyID;
    }

    public void setAgencyID(String agencyID) {
        this.agencyID = agencyID;
    }

    public Contacts getContacts() {
        return contacts;
    }

    public void setContacts(Contacts contacts) {
        this.contacts = contacts;
    }
}