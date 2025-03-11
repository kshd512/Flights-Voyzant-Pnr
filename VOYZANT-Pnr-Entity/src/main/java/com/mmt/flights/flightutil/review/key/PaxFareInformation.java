package com.mmt.flights.flightutil.review.key;

import java.math.BigDecimal;

public class PaxFareInformation {
    private BigDecimal adultTotal;
    private BigDecimal childTotal;
    private BigDecimal infantTotal;
    private BigDecimal adultBase;
    private BigDecimal childBase;
    private BigDecimal infantBase;

    public PaxFareInformation(BigDecimal adultTotal, BigDecimal childTotal, BigDecimal infantTotal,
                            BigDecimal adultBase, BigDecimal childBase, BigDecimal infantBase) {
        this.adultTotal = adultTotal;
        this.childTotal = childTotal;
        this.infantTotal = infantTotal;
        this.adultBase = adultBase;
        this.childBase = childBase;
        this.infantBase = infantBase;
    }

    @Override
    public String toString() {
        return String.format("%s|%s|%s|%s|%s|%s", 
            adultTotal.toString(),
            childTotal.toString(), 
            infantTotal.toString(),
            adultBase.toString(),
            childBase.toString(),
            infantBase.toString());
    }
}