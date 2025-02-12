package com.mmt.flights.entity.pnr.retrieve.response;

public class Tax {
    private String taxCode;
    private Double bookingCurrencyPrice;
    private Double equivCurrencyPrice;

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

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