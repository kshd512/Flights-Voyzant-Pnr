package com.mmt.flights.entity.split.request;

import lombok.Data;

@Data
public class TravelAgencySender {
    private String name;
    private String IATA_Number;
    private String agencyID;
    private Contacts contacts;
}