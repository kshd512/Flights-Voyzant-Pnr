
package com.mmt.flights.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.validation.Valid;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deck",
    "seatSet",
    "propertyList"
})
public class SeatInformation {

    @JsonProperty("deck")
    private Integer deck;
    @JsonProperty("seatSet")
    private Integer seatSet;
    @JsonProperty("propertyList")
    @Valid
    private PropertyList propertyList;

    @JsonProperty("deck")
    public Integer getDeck() {
        return deck;
    }

    @JsonProperty("deck")
    public void setDeck(Integer deck) {
        this.deck = deck;
    }

    @JsonProperty("seatSet")
    public Integer getSeatSet() {
        return seatSet;
    }

    @JsonProperty("seatSet")
    public void setSeatSet(Integer seatSet) {
        this.seatSet = seatSet;
    }

    @JsonProperty("propertyList")
    public PropertyList getPropertyList() {
        return propertyList;
    }

    @JsonProperty("propertyList")
    public void setPropertyList(PropertyList propertyList) {
        this.propertyList = propertyList;
    }

}
