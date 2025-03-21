package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReshopOfferInstance {

    @JsonProperty("ReshopOffer")
    private List<ReshopOffer> reshopOffers;

    public List<ReshopOffer> getReshopOffers() {
        return reshopOffers;
    }

    public void setReshopOffers(List<ReshopOffer> reshopOffers) {
        this.reshopOffers = reshopOffers;
    }
}
