package com.mmt.flights.entity.pnr.retrieve.response;

public class Penalty {
    private Fee changeFee;
    private Fee cancelationFee;

    public Fee getChangeFee() {
        return changeFee;
    }

    public void setChangeFee(Fee changeFee) {
        this.changeFee = changeFee;
    }

    public Fee getCancelationFee() {
        return cancelationFee;
    }

    public void setCancelationFee(Fee cancelationFee) {
        this.cancelationFee = cancelationFee;
    }
}