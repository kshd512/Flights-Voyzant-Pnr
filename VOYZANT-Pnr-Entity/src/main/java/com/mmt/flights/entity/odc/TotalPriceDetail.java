package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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