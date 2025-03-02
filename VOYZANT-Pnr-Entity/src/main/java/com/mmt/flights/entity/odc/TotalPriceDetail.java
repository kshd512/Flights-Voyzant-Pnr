package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TotalPriceDetail {
    @JsonProperty("TotalAmount")
    private PriceInstance totalAmount;

    public PriceInstance getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(PriceInstance totalAmount) {
        this.totalAmount = totalAmount;
    }
}