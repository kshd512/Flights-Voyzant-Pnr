package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order {

    @JsonProperty("TotalPrice")
    private PriceInstance totalPrice;

    @JsonProperty("BasePrice")
    private PriceInstance basePrice;

    @JsonProperty("TaxPrice")
    private PriceInstance taxPrice;

    public PriceInstance getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(PriceInstance totalPrice) {
        this.totalPrice = totalPrice;
    }

    public PriceInstance getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(PriceInstance basePrice) {
        this.basePrice = basePrice;
    }

    public PriceInstance getTaxPrice() {
        return taxPrice;
    }

    public void setTaxPrice(PriceInstance taxPrice) {
        this.taxPrice = taxPrice;
    }
}