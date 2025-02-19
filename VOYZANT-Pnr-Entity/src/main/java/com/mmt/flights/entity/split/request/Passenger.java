package com.mmt.flights.entity.split.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Passenger {
    @JsonProperty("PassengerID")
    private String passengerId;
    
    @JsonProperty("PTC")
    private String ptc;
    
    @JsonProperty("NameTitle")
    private String nameTitle;
    
    @JsonProperty("FirstName")
    private String firstName;
    
    @JsonProperty("MiddleName")
    private String middleName;
    
    @JsonProperty("LastName")
    private String lastName;
}