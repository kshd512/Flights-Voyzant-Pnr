package com.mmt.flights.entity.pnr.retrieve.response;

import java.util.List;

public class Price {
    private PriceDetail totalAmount;
    private PriceDetail baseAmount;
    private PriceDetail taxAmount;
    private Commission commission;
    private PriceDetail bookingFee;
    private PortalCharges portalCharges;
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