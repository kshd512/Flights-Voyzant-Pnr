
package com.mmt.flights.entity.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.validation.Valid;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "seat",
    "travelClass",
    "advancedPreferences"
})
public class SeatPreferences {

    @JsonProperty("seat")
    private Integer seat;
    @JsonProperty("travelClass")
    private Integer travelClass;
    @JsonProperty("advancedPreferences")
    @Valid
    private List<Object> advancedPreferences = null;

    @JsonProperty("seat")
    public Integer getSeat() {
        return seat;
    }

    @JsonProperty("seat")
    public void setSeat(Integer seat) {
        this.seat = seat;
    }

    @JsonProperty("travelClass")
    public Integer getTravelClass() {
        return travelClass;
    }

    @JsonProperty("travelClass")
    public void setTravelClass(Integer travelClass) {
        this.travelClass = travelClass;
    }

    @JsonProperty("advancedPreferences")
    public List<Object> getAdvancedPreferences() {
        return advancedPreferences;
    }

    @JsonProperty("advancedPreferences")
    public void setAdvancedPreferences(List<Object> advancedPreferences) {
        this.advancedPreferences = advancedPreferences;
    }

}
