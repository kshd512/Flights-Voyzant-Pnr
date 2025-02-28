package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Contact {
    @JsonProperty("Contact")
    private List<EmailContact> contact = new ArrayList<>();

    public List<EmailContact> getContact() {
        return contact;
    }

    public void setContact(List<EmailContact> contact) {
        this.contact = contact;
    }
}