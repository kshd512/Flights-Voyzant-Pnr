package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Price {

    @JsonProperty("TotalAmount")
    private PriceInstance totalAmount;

    @JsonProperty("BaseAmount")
    private PriceInstance BaseAmount;

    @JsonProperty("TaxAmount")
    private PriceInstance TaxAmount;

    @JsonProperty("BookingFee")
    private PriceInstance bookingFee;

    public PriceInstance getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(PriceInstance totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PriceInstance getBaseAmount() {
        return BaseAmount;
    }

    public void setBaseAmount(PriceInstance baseAmount) {
        BaseAmount = baseAmount;
    }

    public PriceInstance getTaxAmount() {
        return TaxAmount;
    }

    public void setTaxAmount(PriceInstance taxAmount) {
        TaxAmount = taxAmount;
    }

    public PriceInstance getBookingFee() {
        return bookingFee;
    }

    public void setBookingFee(PriceInstance bookingFee) {
        this.bookingFee = bookingFee;
    }
}