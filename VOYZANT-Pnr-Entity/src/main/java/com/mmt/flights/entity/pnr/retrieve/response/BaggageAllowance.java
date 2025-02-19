package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BaggageAllowance {
    @JsonProperty("BaggageAllowanceID")
    private String baggageAllowanceID;
    
    @JsonProperty("BaggageCategory")
    private String baggageCategory;
    
    @JsonProperty("AllowanceDescription")
    private AllowanceDescription allowanceDescription;
    
    @JsonProperty("PieceAllowance")
    private PieceAllowance pieceAllowance;

    public String getBaggageAllowanceID() {
        return baggageAllowanceID;
    }

    public void setBaggageAllowanceID(String baggageAllowanceID) {
        this.baggageAllowanceID = baggageAllowanceID;
    }

    public String getBaggageCategory() {
        return baggageCategory;
    }

    public void setBaggageCategory(String baggageCategory) {
        this.baggageCategory = baggageCategory;
    }

    public AllowanceDescription getAllowanceDescription() {
        return allowanceDescription;
    }

    public void setAllowanceDescription(AllowanceDescription allowanceDescription) {
        this.allowanceDescription = allowanceDescription;
    }

    public PieceAllowance getPieceAllowance() {
        return pieceAllowance;
    }

    public void setPieceAllowance(PieceAllowance pieceAllowance) {
        this.pieceAllowance = pieceAllowance;
    }
}
