package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Preference {
    @JsonProperty("WheelChairPreference")
    private WheelChairPreference wheelChairPreference;
    @JsonProperty("SeatPreference")
    private String seatPreference;

    public WheelChairPreference getWheelChairPreference() {
        return wheelChairPreference;
    }

    public void setWheelChairPreference(WheelChairPreference wheelChairPreference) {
        this.wheelChairPreference = wheelChairPreference;
    }

    public String getSeatPreference() {
        return seatPreference;
    }

    public void setSeatPreference(String seatPreference) {
        this.seatPreference = seatPreference;
    }
}