package com.mmt.flights.entity.pnr.retrieve.response;

public class FareComponent {
    private String priceClassRef;
    private String segmentRefs;
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