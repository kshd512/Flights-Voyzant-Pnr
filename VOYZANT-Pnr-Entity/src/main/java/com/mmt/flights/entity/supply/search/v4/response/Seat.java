
package com.mmt.flights.entity.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.validation.Valid;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "compartmentDesignator",
    "penalty",
    "unitDesignator",
    "seatInformation",
    "arrivalStation",
    "departureStation",
    "passengerKey",
    "unitKey"
})
public class Seat {

    @JsonProperty("compartmentDesignator")
    private String compartmentDesignator;
    @JsonProperty("penalty")
    private Integer penalty;
    @JsonProperty("unitDesignator")
    private String unitDesignator;
    @JsonProperty("seatInformation")
    @Valid
    private SeatInformation seatInformation;
    @JsonProperty("arrivalStation")
    private String arrivalStation;
    @JsonProperty("departureStation")
    private String departureStation;
    @JsonProperty("passengerKey")
    private String passengerKey;
    @JsonProperty("unitKey")
    private String unitKey;

    @JsonProperty("compartmentDesignator")
    public String getCompartmentDesignator() {
        return compartmentDesignator;
    }

    @JsonProperty("compartmentDesignator")
    public void setCompartmentDesignator(String compartmentDesignator) {
        this.compartmentDesignator = compartmentDesignator;
    }

    @JsonProperty("penalty")
    public Integer getPenalty() {
        return penalty;
    }

    @JsonProperty("penalty")
    public void setPenalty(Integer penalty) {
        this.penalty = penalty;
    }

    @JsonProperty("unitDesignator")
    public String getUnitDesignator() {
        return unitDesignator;
    }

    @JsonProperty("unitDesignator")
    public void setUnitDesignator(String unitDesignator) {
        this.unitDesignator = unitDesignator;
    }

    @JsonProperty("seatInformation")
    public SeatInformation getSeatInformation() {
        return seatInformation;
    }

    @JsonProperty("seatInformation")
    public void setSeatInformation(SeatInformation seatInformation) {
        this.seatInformation = seatInformation;
    }

    @JsonProperty("arrivalStation")
    public String getArrivalStation() {
        return arrivalStation;
    }

    @JsonProperty("arrivalStation")
    public void setArrivalStation(String arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    @JsonProperty("departureStation")
    public String getDepartureStation() {
        return departureStation;
    }

    @JsonProperty("departureStation")
    public void setDepartureStation(String departureStation) {
        this.departureStation = departureStation;
    }

    @JsonProperty("passengerKey")
    public String getPassengerKey() {
        return passengerKey;
    }

    @JsonProperty("passengerKey")
    public void setPassengerKey(String passengerKey) {
        this.passengerKey = passengerKey;
    }

    @JsonProperty("unitKey")
    public String getUnitKey() {
        return unitKey;
    }

    @JsonProperty("unitKey")
    public void setUnitKey(String unitKey) {
        this.unitKey = unitKey;
    }

}
