package com.mmt.flights.entity.pnr.retrieve.response;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Contacts {
    @JsonProperty("Contact")
    private List<Contact> contact;

    public List<Contact> getContact() {
        return contact;
    }

    public void setContact(List<Contact> contact) {
        this.contact = contact;
    }
}