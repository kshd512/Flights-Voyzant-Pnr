package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fee {
    @JsonProperty("Before")
    private PriceDetail before;
    @JsonProperty("After")
    private PriceDetail after;

    public PriceDetail getBefore() {
        return before;
    }

    public void setBefore(PriceDetail before) {
        this.before = before;
    }

    public PriceDetail getAfter() {
        return after;
    }

    public void setAfter(PriceDetail after) {
        this.after = after;
    }
}