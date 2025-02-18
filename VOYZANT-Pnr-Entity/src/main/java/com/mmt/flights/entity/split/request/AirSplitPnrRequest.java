package com.mmt.flights.entity.split.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AirSplitPnrRequest {
    @JsonProperty("AirSplitPnrRQ")
    private AirSplitPnrRQ airSplitPnrRQ;
}