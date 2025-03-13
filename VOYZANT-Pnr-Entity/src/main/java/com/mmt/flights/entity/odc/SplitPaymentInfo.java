package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SplitPaymentInfo {
    @JsonProperty("AirItineraryId")
    private String airItineraryId;

    @JsonProperty("MultipleFop")
    private String multipleFop;

    @JsonProperty("MaxCardsPerPax")
    private int maxCardsPerPax;

    @JsonProperty("MaxCardsPerPaxInMFOP")
    private int maxCardsPerPaxInMFOP;

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

    public int getMaxCardsPerPax() {
        return maxCardsPerPax;
    }

    public void setMaxCardsPerPax(int maxCardsPerPax) {
        this.maxCardsPerPax = maxCardsPerPax;
    }

    public int getMaxCardsPerPaxInMFOP() {
        return maxCardsPerPaxInMFOP;
    }

    public void setMaxCardsPerPaxInMFOP(int maxCardsPerPaxInMFOP) {
        this.maxCardsPerPaxInMFOP = maxCardsPerPaxInMFOP;
    }
}