package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WheelChairPreference {
    @JsonProperty("Reason")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}