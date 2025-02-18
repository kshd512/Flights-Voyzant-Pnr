package com.mmt.flights.entity.split.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TravelAgencySender {
    @JsonProperty("Name")
    private String name;
    
    @JsonProperty("IATA_Number")
    private String iataNumber;
    
    @JsonProperty("AgencyID")
    private String agencyId;
    
    @JsonProperty("Contacts")
    private Contacts contacts;
}