package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceContainer {

    @JsonProperty("TotalPrice")
    private PriceInstance totalPrice;

    public PriceInstance getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(PriceInstance totalPrice) {
        this.totalPrice = totalPrice;
    }
}