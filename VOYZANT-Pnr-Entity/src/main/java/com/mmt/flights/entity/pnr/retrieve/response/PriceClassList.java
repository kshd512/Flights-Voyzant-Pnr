package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceClassList {
    @JsonProperty("PriceClass")
    private List<PriceClass> priceClass;

    public List<PriceClass> getPriceClass() {
        return priceClass;
    }

    public void setPriceClass(List<PriceClass> priceClass) {
        this.priceClass = priceClass;
    }
}