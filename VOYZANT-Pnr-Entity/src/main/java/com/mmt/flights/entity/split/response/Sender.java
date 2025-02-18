package com.mmt.flights.entity.split.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Sender {
    @JsonProperty("TravelAgencySender")
    private TravelAgencySender travelAgencySender;
}