package com.mmt.flights.entity.split.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Contacts {
    @JsonProperty("Contact")
    private List<Contact> contact;
}