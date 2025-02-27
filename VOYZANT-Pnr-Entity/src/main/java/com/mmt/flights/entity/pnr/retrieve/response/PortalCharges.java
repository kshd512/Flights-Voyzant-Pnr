package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PortalCharges {
    @JsonProperty("Markup")
    private PriceDetail markup;
    @JsonProperty("Surcharge")
    private PriceDetail surcharge;
    @JsonProperty("Discount")
    private PriceDetail discount;

    public PriceDetail getMarkup() {
        return markup;
    }

    public void setMarkup(PriceDetail markup) {
        this.markup = markup;
    }

    public PriceDetail getSurcharge() {
        return surcharge;
    }

    public void setSurcharge(PriceDetail surcharge) {
        this.surcharge = surcharge;
    }

    public PriceDetail getDiscount() {
        return discount;
    }

    public void setDiscount(PriceDetail discount) {
        this.discount = discount;
    }
}