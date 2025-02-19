package com.mmt.flights.entity.cancel.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

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