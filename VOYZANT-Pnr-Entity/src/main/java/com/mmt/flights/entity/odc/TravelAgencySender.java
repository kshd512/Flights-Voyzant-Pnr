package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TravelAgencySender {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("IATA_Number")
    private String IATA_Number = "";

    @JsonProperty("AgencyID")
    private String agencyID;

    @JsonProperty("Contacts")
    private Contact contacts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIATA_Number() {
        return IATA_Number;
    }

    public void setIATA_Number(String IATA_Number) {
        this.IATA_Number = IATA_Number;
    }

    public String getAgencyID() {
        return agencyID;
    }

    public void setAgencyID(String agencyID) {
        this.agencyID = agencyID;
    }

    public Contact getContacts() {
        return contacts;
    }

    public void setContacts(Contact contacts) {
        this.contacts = contacts;
    }
}