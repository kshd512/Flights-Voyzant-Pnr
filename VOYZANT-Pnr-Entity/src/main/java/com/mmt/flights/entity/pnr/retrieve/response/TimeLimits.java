package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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