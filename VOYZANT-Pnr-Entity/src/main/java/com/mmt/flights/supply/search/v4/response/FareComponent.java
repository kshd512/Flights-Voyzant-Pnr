package com.mmt.flights.supply.search.v4.response;

import java.util.List;

public class FareComponent {
    private String fareBasisCode;
    private String classOfService;
    private String productClass;
    private List<PassengerFare> passengerFares;

    public String getFareBasisCode() {
        return fareBasisCode;
    }

    public void setFareBasisCode(String fareBasisCode) {
        this.fareBasisCode = fareBasisCode;
    }

    public String getClassOfService() {
        return classOfService;
    }

    public void setClassOfService(String classOfService) {
        this.classOfService = classOfService;
    }

    public String getProductClass() {
        return productClass;
    }

    public void setProductClass(String productClass) {
        this.productClass = productClass;
    }

    public List<PassengerFare> getPassengerFares() {
        return passengerFares;
    }

    public void setPassengerFares(List<PassengerFare> passengerFares) {
        this.passengerFares = passengerFares;
    }
}