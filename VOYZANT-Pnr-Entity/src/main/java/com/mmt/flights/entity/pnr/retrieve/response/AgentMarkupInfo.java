package com.mmt.flights.entity.pnr.retrieve.response;

public class AgentMarkupInfo {
    private PriceDetail onflyMarkup;
    private PriceDetail onflyDiscount;
    private PriceDetail onflyHst;
    private PromoDiscount promoDiscount;

    public PriceDetail getOnflyMarkup() {
        return onflyMarkup;
    }

    public void setOnflyMarkup(PriceDetail onflyMarkup) {
        this.onflyMarkup = onflyMarkup;
    }

    public PriceDetail getOnflyDiscount() {
        return onflyDiscount;
    }

    public void setOnflyDiscount(PriceDetail onflyDiscount) {
        this.onflyDiscount = onflyDiscount;
    }

    public PriceDetail getOnflyHst() {
        return onflyHst;
    }

    public void setOnflyHst(PriceDetail onflyHst) {
        this.onflyHst = onflyHst;
    }

    public PromoDiscount getPromoDiscount() {
        return promoDiscount;
    }

    public void setPromoDiscount(PromoDiscount promoDiscount) {
        this.promoDiscount = promoDiscount;
    }
}