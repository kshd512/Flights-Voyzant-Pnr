package com.mmt.flights.entity.pnr.retrieve.response;

public class FareBasis {
    private FareBasisCode fareBasisCode;
    private String rbd;
    private String cabinType;
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