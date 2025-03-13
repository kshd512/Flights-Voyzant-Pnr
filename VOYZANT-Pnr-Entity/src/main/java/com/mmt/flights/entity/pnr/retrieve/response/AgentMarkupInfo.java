package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentMarkupInfo {
    @JsonProperty("OnflyMarkup")
    private PriceDetail onflyMarkup;
    @JsonProperty("OnflyDiscount")
    private PriceDetail onflyDiscount;
    @JsonProperty("OnflyHst")
    private PriceDetail onflyHst;
    @JsonProperty("PromoDiscount")
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