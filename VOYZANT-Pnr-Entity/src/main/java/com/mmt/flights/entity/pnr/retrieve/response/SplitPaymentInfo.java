package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SplitPaymentInfo {
    @JsonProperty("AirItineraryId")
    private String airItineraryId;
    @JsonProperty("MultipleFop")
    private String multipleFop;
    @JsonProperty("MaxCardsPerPax")
    private Integer maxCardsPerPax;
    @JsonProperty("MaxCardsPerPaxInMFOP")
    private Integer maxCardsPerPaxInMFOP;

    public String getAirItineraryId() {
        return airItineraryId;
    }

    public void setAirItineraryId(String airItineraryId) {
        this.airItineraryId = airItineraryId;
    }

    public String getMultipleFop() {
        return multipleFop;
    }

    public void setMultipleFop(String multipleFop) {
        this.multipleFop = multipleFop;
    }

    public Integer getMaxCardsPerPax() {
        return maxCardsPerPax;
    }

    public void setMaxCardsPerPax(Integer maxCardsPerPax) {
        this.maxCardsPerPax = maxCardsPerPax;
    }

    public Integer getMaxCardsPerPaxInMFOP() {
        return maxCardsPerPaxInMFOP;
    }

    public void setMaxCardsPerPaxInMFOP(Integer maxCardsPerPaxInMFOP) {
        this.maxCardsPerPaxInMFOP = maxCardsPerPaxInMFOP;
    }
}
