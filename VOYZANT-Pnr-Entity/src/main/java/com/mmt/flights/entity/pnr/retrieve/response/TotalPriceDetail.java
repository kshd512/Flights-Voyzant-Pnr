package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TotalPriceDetail {
    @JsonProperty("TotalAmount")
    private PriceDetail TotalAmount;

    public PriceDetail getTotalAmount() {
        return TotalAmount;
    }

    public void setTotalAmount(PriceDetail totalAmount) {
        TotalAmount = totalAmount;
    }
}
