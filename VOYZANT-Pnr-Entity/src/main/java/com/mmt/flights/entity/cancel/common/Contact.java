package com.mmt.flights.entity.cancel.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Contact {
    @JsonProperty("EmailContact")
    private String emailContact;

    public String getEmailContact() {
        return emailContact;
    }

    public void setEmailContact(String emailContact) {
        this.emailContact = emailContact;
    }
}