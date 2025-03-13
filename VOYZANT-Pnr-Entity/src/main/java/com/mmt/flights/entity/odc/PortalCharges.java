package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortalCharges {
    @JsonProperty("Markup")
    private Price markup;

    @JsonProperty("Surcharge")
    private Price surcharge;

    @JsonProperty("Discount")
    private Price discount;

    public Price getMarkup() {
        return markup;
    }

    public void setMarkup(Price markup) {
        this.markup = markup;
    }

    public Price getSurcharge() {
        return surcharge;
    }

    public void setSurcharge(Price surcharge) {
        this.surcharge = surcharge;
    }

    public Price getDiscount() {
        return discount;
    }

    public void setDiscount(Price discount) {
        this.discount = discount;
    }
}