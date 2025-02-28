package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeLimits {
    @JsonProperty("OfferExpirationDateTime")
    private String offerExpirationDateTime;

    @JsonProperty("PaymentExpirationDateTime")
    private String paymentExpirationDateTime;

    public String getOfferExpirationDateTime() {
        return offerExpirationDateTime;
    }

    public void setOfferExpirationDateTime(String offerExpirationDateTime) {
        this.offerExpirationDateTime = offerExpirationDateTime;
    }

    public String getPaymentExpirationDateTime() {
        return paymentExpirationDateTime;
    }

    public void setPaymentExpirationDateTime(String paymentExpirationDateTime) {
        this.paymentExpirationDateTime = paymentExpirationDateTime;
    }
}