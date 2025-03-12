
package com.mmt.flights.entity.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Totals {

    @JsonProperty("fareTotal")
    private double fareTotal;
    @JsonProperty("revenueTotal")
    private double revenueTotal;
    @JsonProperty("publishedTotal")
    private double publishedTotal;
    @JsonProperty("loyaltyTotal")
    private double loyaltyTotal;
    @JsonProperty("discountedTotal")
    private double discountedTotal;

    @JsonProperty("fareTotal")
    public double getFareTotal() {
        return fareTotal;
    }

    @JsonProperty("fareTotal")
    public void setFareTotal(double fareTotal) {
        this.fareTotal = fareTotal;
    }

    @JsonProperty("revenueTotal")
    public double getRevenueTotal() {
        return revenueTotal;
    }

    @JsonProperty("revenueTotal")
    public void setRevenueTotal(double revenueTotal) {
        this.revenueTotal = revenueTotal;
    }

    @JsonProperty("publishedTotal")
    public double getPublishedTotal() {
        return publishedTotal;
    }

    @JsonProperty("publishedTotal")
    public void setPublishedTotal(double publishedTotal) {
        this.publishedTotal = publishedTotal;
    }

    @JsonProperty("loyaltyTotal")
    public double getLoyaltyTotal() {
        return loyaltyTotal;
    }

    @JsonProperty("loyaltyTotal")
    public void setLoyaltyTotal(double loyaltyTotal) {
        this.loyaltyTotal = loyaltyTotal;
    }

    @JsonProperty("discountedTotal")
    public double getDiscountedTotal() {
        return discountedTotal;
    }

    @JsonProperty("discountedTotal")
    public void setDiscountedTotal(double discountedTotal) {
        this.discountedTotal = discountedTotal;
    }

}
