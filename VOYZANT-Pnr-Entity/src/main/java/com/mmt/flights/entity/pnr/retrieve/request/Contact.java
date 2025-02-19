package com.mmt.flights.entity.pnr.retrieve.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Contact {
    @JsonProperty("emailcontact")
    private String emailContact;

    public String getEmailContact() {
        return emailContact;
    }

    public void setEmailContact(String emailContact) {
        this.emailContact = emailContact;
    }
}