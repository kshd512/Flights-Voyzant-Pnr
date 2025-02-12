package com.mmt.flights.entity.pnr.retrieve.response;

import java.util.List;

public class Payments {
    private List<Payment> payment;

    public List<Payment> getPayment() {
        return payment;
    }

    public void setPayment(List<Payment> payment) {
        this.payment = payment;
    }
}