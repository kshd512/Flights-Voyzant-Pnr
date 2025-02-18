package com.mmt.flights.entity.split.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Party {
    @JsonProperty("Sender")
    private Sender sender;
}