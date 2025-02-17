package com.mmt.flights.entity.split.request;

import lombok.Data;

@Data
public class Passenger {
    private String passengerID;
    private String PTC;
    private String nameTitle;
    private String firstName;
    private String middleName;
    private String lastName;
}