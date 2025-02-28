package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TotalPriceDetail {
    @JsonProperty("TotalAmount")
    private Price totalAmount;

    public Price getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Price totalAmount) {
        this.totalAmount = totalAmount;
    }
}