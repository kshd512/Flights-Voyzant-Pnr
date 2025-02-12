package com.mmt.flights.entity.pnr.retrieve.response;

public class PriceDetail {
    private Double bookingCurrencyPrice;
    private Double equivCurrencyPrice;

    public Double getBookingCurrencyPrice() {
        return bookingCurrencyPrice;
    }

    public void setBookingCurrencyPrice(Double bookingCurrencyPrice) {
        this.bookingCurrencyPrice = bookingCurrencyPrice;
    }

    public Double getEquivCurrencyPrice() {
        return equivCurrencyPrice;
    }

    public void setEquivCurrencyPrice(Double equivCurrencyPrice) {
        this.equivCurrencyPrice = equivCurrencyPrice;
    }
}