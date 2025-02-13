package com.mmt.flights.entity.pnr.retrieve.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TravelAgencySender {
    @JsonProperty("Name")
    private String Name;
    
    @JsonProperty("IATA_Number")
    private String Iata_number;
    
    @JsonProperty("AgencyID")
    private String Agencyid;
    
    @JsonProperty("Contacts")
    private Contacts Contacts;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getIata_number() {
        return Iata_number;
    }

    public void setIata_number(String iata_number) {
        Iata_number = iata_number;
    }

    public String getAgencyid() {
        return Agencyid;
    }

    public void setAgencyid(String agencyid) {
        Agencyid = agencyid;
    }

    public Contacts getContacts() {
        return Contacts;
    }

    public void setContacts(Contacts contacts) {
        Contacts = contacts;
    }
}
