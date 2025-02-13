package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FareComponent {
    @JsonProperty("PriceClassRef")
    private String priceClassRef;
    @JsonProperty("SegmentRefs")
    private String segmentRefs;
    @JsonProperty("FareBasis")
    private FareBasis fareBasis;

    public String getPriceClassRef() {
        return priceClassRef;
    }

    public void setPriceClassRef(String priceClassRef) {
        this.priceClassRef = priceClassRef;
    }

    public String getSegmentRefs() {
        return segmentRefs;
    }

    public void setSegmentRefs(String segmentRefs) {
        this.segmentRefs = segmentRefs;
    }

    public FareBasis getFareBasis() {
        return fareBasis;
    }

    public void setFareBasis(FareBasis fareBasis) {
        this.fareBasis = fareBasis;
    }
}