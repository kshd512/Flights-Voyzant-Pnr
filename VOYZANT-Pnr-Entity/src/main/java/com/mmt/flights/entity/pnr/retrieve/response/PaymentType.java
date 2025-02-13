package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

class PaymentType {
    @JsonProperty("F")
    private PriceDetail f;
    @JsonProperty("P")
    private Double p;
    @JsonProperty("Charges")
    private PriceDetail charges;

    public PriceDetail getF() {
        return f;
    }

    public void setF(PriceDetail f) {
        this.f = f;
    }

    public Double getP() {
        return p;
    }

    public void setP(Double p) {
        this.p = p;
    }

    public PriceDetail getCharges() {
        return charges;
    }

    public void setCharges(PriceDetail charges) {
        this.charges = charges;
    }
}