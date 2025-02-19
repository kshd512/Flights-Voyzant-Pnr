package com.mmt.flights.entity.pnr.retrieve.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TravelAgencySender {
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("iata_number")
    private String iataNumber;
    
    @JsonProperty("agencyid")
    private String agencyId;
    
    @JsonProperty("contacts")
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

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public Contacts getContacts() {
        return contacts;
    }

    public void setContacts(Contacts contacts) {
        this.contacts = contacts;
    }
}
