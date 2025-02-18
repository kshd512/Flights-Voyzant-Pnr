package com.mmt.flights.entity.split.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DataLists {
    @JsonProperty("PassengerList")
    private PassengerList passengerList;
}