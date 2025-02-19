package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TimeLimits {
    @JsonProperty("OfferExpirationDateTime")
    private String offerExpirationDateTime;

    public String getOfferExpirationDateTime() {
        return offerExpirationDateTime;
    }

    public void setOfferExpirationDateTime(String offerExpirationDateTime) {
        this.offerExpirationDateTime = offerExpirationDateTime;
    }
}