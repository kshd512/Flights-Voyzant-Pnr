package com.mmt.flights.entity.split.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AirSplitPnrResponse {
    @JsonProperty("AirSplitPnrRS")
    private AirSplitPnrRS airSplitPnrRS;

    public AirSplitPnrRS getAirSplitPnrRS() {
        return airSplitPnrRS;
    }

    public void setAirSplitPnrRS(AirSplitPnrRS airSplitPnrRS) {
        this.airSplitPnrRS = airSplitPnrRS;
    }
}