package com.mmt.flights.entity.pnr.retrieve.response;

public class Fee {
    private PriceDetail before;
    private PriceDetail after;

    public PriceDetail getBefore() {
        return before;
    }

    public void setBefore(PriceDetail before) {
        this.before = before;
    }

    public PriceDetail getAfter() {
        return after;
    }

    public void setAfter(PriceDetail after) {
        this.after = after;
    }
}