package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Price {
    @JsonProperty("TotalAmount")
    private PriceDetail totalAmount;
    @JsonProperty("BaseAmount")
    private PriceDetail baseAmount;
    @JsonProperty("TaxAmount")
    private PriceDetail taxAmount;
    @JsonProperty("Commission")
    private Commission commission;
    @JsonProperty("BookingFee")
    private PriceDetail bookingFee;
    @JsonProperty("PortalCharges")
    private PortalCharges portalCharges;
    @JsonProperty("Taxes")
    private List<Tax> taxes;

    public PriceDetail getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(PriceDetail totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PriceDetail getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(PriceDetail baseAmount) {
        this.baseAmount = baseAmount;
    }

    public PriceDetail getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(PriceDetail taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Commission getCommission() {
        return commission;
    }

    public void setCommission(Commission commission) {
        this.commission = commission;
    }

    public PriceDetail getBookingFee() {
        return bookingFee;
    }

    public void setBookingFee(PriceDetail bookingFee) {
        this.bookingFee = bookingFee;
    }

    public PortalCharges getPortalCharges() {
        return portalCharges;
    }

    public void setPortalCharges(PortalCharges portalCharges) {
        this.portalCharges = portalCharges;
    }

    public List<Tax> getTaxes() {
        return taxes;
    }

    public void setTaxes(List<Tax> taxes) {
        this.taxes = taxes;
    }
}