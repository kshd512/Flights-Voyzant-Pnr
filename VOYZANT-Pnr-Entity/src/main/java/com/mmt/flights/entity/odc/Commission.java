package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Commission {
    @JsonProperty("AgencyCommission")
    private Price agencyCommission;

    @JsonProperty("AgencyYqCommission")
    private Price agencyYqCommission;

    public Price getAgencyCommission() {
        return agencyCommission;
    }

    public void setAgencyCommission(Price agencyCommission) {
        this.agencyCommission = agencyCommission;
    }

    public Price getAgencyYqCommission() {
        return agencyYqCommission;
    }

    public void setAgencyYqCommission(Price agencyYqCommission) {
        this.agencyYqCommission = agencyYqCommission;
    }
}