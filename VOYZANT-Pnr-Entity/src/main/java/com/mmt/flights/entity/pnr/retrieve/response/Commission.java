package com.mmt.flights.entity.pnr.retrieve.response;

public class Commission {
    private PriceDetail agencyCommission;
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