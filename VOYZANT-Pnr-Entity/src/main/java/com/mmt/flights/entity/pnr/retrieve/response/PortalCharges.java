package com.mmt.flights.entity.pnr.retrieve.response;

public class PortalCharges {
    private PriceDetail markup;
    private PriceDetail surcharge;
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