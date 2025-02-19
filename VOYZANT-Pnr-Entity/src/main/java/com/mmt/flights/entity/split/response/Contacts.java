package com.mmt.flights.entity.split.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
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