package com.mmt.flights.entity.pnr.retrieve.request;

class TravelAgencySender {
    private String name;
    private String iata_number;
    private String agencyid;
    private Contacts contacts;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIata_number() {
        return iata_number;
    }

    public void setIata_number(String iata_number) {
        this.iata_number = iata_number;
    }

    public String getAgencyid() {
        return agencyid;
    }

    public void setAgencyid(String agencyid) {
        this.agencyid = agencyid;
    }

    public Contacts getContacts() {
        return contacts;
    }

    public void setContacts(Contacts contacts) {
        this.contacts = contacts;
    }
}
