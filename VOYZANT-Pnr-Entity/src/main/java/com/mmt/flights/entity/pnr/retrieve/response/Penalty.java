package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Penalty {
    @JsonProperty("ChangeFee")
    private Fee changeFee;
    @JsonProperty("CancelationFee")
    private Fee cancelationFee;

    public Fee getChangeFee() {
        return changeFee;
    }

    public void setChangeFee(Fee changeFee) {
        this.changeFee = changeFee;
    }

    public Fee getCancelationFee() {
        return cancelationFee;
    }

    public void setCancelationFee(Fee cancelationFee) {
        this.cancelationFee = cancelationFee;
    }
}