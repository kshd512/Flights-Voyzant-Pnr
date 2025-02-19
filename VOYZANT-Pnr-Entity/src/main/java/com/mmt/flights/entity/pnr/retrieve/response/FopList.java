package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class FopList {
    @JsonProperty("CC")
    private PaymentMethod cc;
    @JsonProperty("DC")
    private PaymentMethod dc;
    @JsonProperty("CASH")
    private PaymentMethod cash;
    @JsonProperty("CHEQUE")
    private PaymentMethod cheque;
    @JsonProperty("ACH")
    private PaymentMethod ach;
    @JsonProperty("PG")
    private PaymentMethod pg;
    @JsonProperty("FopKey")
    private String fopKey;

    public PaymentMethod getCc() {
        return cc;
    }

    public void setCc(PaymentMethod cc) {
        this.cc = cc;
    }

    public PaymentMethod getDc() {
        return dc;
    }

    public void setDc(PaymentMethod dc) {
        this.dc = dc;
    }

    public PaymentMethod getCash() {
        return cash;
    }

    public void setCash(PaymentMethod cash) {
        this.cash = cash;
    }

    public PaymentMethod getCheque() {
        return cheque;
    }

    public void setCheque(PaymentMethod cheque) {
        this.cheque = cheque;
    }

    public PaymentMethod getAch() {
        return ach;
    }

    public void setAch(PaymentMethod ach) {
        this.ach = ach;
    }

    public PaymentMethod getPg() {
        return pg;
    }

    public void setPg(PaymentMethod pg) {
        this.pg = pg;
    }

    public String getFopKey() {
        return fopKey;
    }

    public void setFopKey(String fopKey) {
        this.fopKey = fopKey;
    }
}