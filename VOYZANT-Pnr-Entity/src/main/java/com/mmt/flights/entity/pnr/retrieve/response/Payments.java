package com.mmt.flights.entity.pnr.retrieve.response;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Payments {
    @JsonProperty("Payment")
    private List<Payment> payment;

    public List<Payment> getPayment() {
        return payment;
    }

    public void setPayment(List<Payment> payment) {
        this.payment = payment;
    }
}