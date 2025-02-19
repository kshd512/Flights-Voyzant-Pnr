package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FareBasis {
    @JsonProperty("FareBasisCode")
    private FareBasisCode fareBasisCode;
    @JsonProperty("RBD")
    private String rbd;
    @JsonProperty("CabinType")
    private String cabinType;
    @JsonProperty("SeatLeft")
    private String seatLeft;

    public FareBasisCode getFareBasisCode() {
        return fareBasisCode;
    }

    public void setFareBasisCode(FareBasisCode fareBasisCode) {
        this.fareBasisCode = fareBasisCode;
    }

    public String getRbd() {
        return rbd;
    }

    public void setRbd(String rbd) {
        this.rbd = rbd;
    }

    public String getCabinType() {
        return cabinType;
    }

    public void setCabinType(String cabinType) {
        this.cabinType = cabinType;
    }

    public String getSeatLeft() {
        return seatLeft;
    }

    public void setSeatLeft(String seatLeft) {
        this.seatLeft = seatLeft;
    }
}