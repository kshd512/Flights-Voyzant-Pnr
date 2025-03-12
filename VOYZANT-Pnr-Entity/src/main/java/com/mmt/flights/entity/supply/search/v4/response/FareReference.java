
package com.mmt.flights.entity.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.soap.Detail;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FareReference {

    @JsonProperty("fareAvailabilityKey")
    private String fareAvailabilityKey;
    @JsonProperty("details")
    private List<Detail> details = null;
    @JsonProperty("isSumOfSector")
    private boolean isSumOfSector;

    @JsonProperty("fareAvailabilityKey")
    public String getFareAvailabilityKey() {
        return fareAvailabilityKey;
    }

    @JsonProperty("fareAvailabilityKey")
    public void setFareAvailabilityKey(String fareAvailabilityKey) {
        this.fareAvailabilityKey = fareAvailabilityKey;
    }

    @JsonProperty("details")
    public List<Detail> getDetails() {
        return details;
    }

    @JsonProperty("details")
    public void setDetails(List<Detail> details) {
        this.details = details;
    }

    @JsonProperty("isSumOfSector")
    public boolean isIsSumOfSector() {
        return isSumOfSector;
    }

    @JsonProperty("isSumOfSector")
    public void setIsSumOfSector(boolean isSumOfSector) {
        this.isSumOfSector = isSumOfSector;
    }

}
