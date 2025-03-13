package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReshopDifferential {

    @JsonProperty("OriginalOrder")
    private Order originalOrder;

    @JsonProperty("NewOffer")
    private Order newOffer;

    @JsonProperty("PenaltyAmount")
    private PriceContainer penaltyAmount;

    @JsonProperty("ReshopDue")
    private PriceContainer reshopDue;

    public Order getOriginalOrder() {
        return originalOrder;
    }

    public void setOriginalOrder(Order originalOrder) {
        this.originalOrder = originalOrder;
    }

    public Order getNewOffer() {
        return newOffer;
    }

    public void setNewOffer(Order newOffer) {
        this.newOffer = newOffer;
    }

    public PriceContainer getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(PriceContainer penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public PriceContainer getReshopDue() {
        return reshopDue;
    }

    public void setReshopDue(PriceContainer reshopDue) {
        this.reshopDue = reshopDue;
    }
}