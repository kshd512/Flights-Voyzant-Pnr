package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Commission {
    @JsonProperty("AgencyCommission")
    private PriceDetail agencyCommission;
    @JsonProperty("AgencyYqCommission")
    private PriceDetail agencyYqCommission;

    public PriceDetail getAgencyCommission() {
        return agencyCommission;
    }

    public void setAgencyCommission(PriceDetail agencyCommission) {
        this.agencyCommission = agencyCommission;
    }

    public PriceDetail getAgencyYqCommission() {
        return agencyYqCommission;
    }

    public void setAgencyYqCommission(PriceDetail agencyYqCommission) {
        this.agencyYqCommission = agencyYqCommission;
    }
}