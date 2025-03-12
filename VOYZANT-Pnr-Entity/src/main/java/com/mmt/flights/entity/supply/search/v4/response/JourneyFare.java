
package com.mmt.flights.entity.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JourneyFare {

    @JsonProperty("totals")
    private Totals totals;
    @JsonProperty("isSumOfSector")
    private boolean isSumOfSector;
    @JsonProperty("fareAvailabilityKey")
    private String fareAvailabilityKey;
    @JsonProperty("fares")
    private List<Fare> fares = null;

    @JsonProperty("totals")
    public Totals getTotals() {
        return totals;
    }

    @JsonProperty("totals")
    public void setTotals(Totals totals) {
        this.totals = totals;
    }

    @JsonProperty("isSumOfSector")
    public boolean isIsSumOfSector() {
        return isSumOfSector;
    }

    @JsonProperty("isSumOfSector")
    public void setIsSumOfSector(boolean isSumOfSector) {
        this.isSumOfSector = isSumOfSector;
    }

    @JsonProperty("fareAvailabilityKey")
    public String getFareAvailabilityKey() {
        return fareAvailabilityKey;
    }

    @JsonProperty("fareAvailabilityKey")
    public void setFareAvailabilityKey(String fareAvailabilityKey) {
        this.fareAvailabilityKey = fareAvailabilityKey;
    }

    @JsonProperty("fares")
    public List<Fare> getFares() {
        return fares;
    }

    @JsonProperty("fares")
    public void setFares(List<Fare> fares) {
        this.fares = fares;
    }

}
