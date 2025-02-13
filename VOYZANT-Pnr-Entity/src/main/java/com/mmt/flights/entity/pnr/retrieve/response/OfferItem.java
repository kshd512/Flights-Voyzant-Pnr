package com.mmt.flights.entity.pnr.retrieve.response;

import java.util.List;

public class OfferItem {
    private String offerItemID;
    private String refundable;
    private String passengerType;
    private Integer passengerQuantity;
    private TotalPriceDetail totalPriceDetail;
    private List<Service> service;
    private FareDetail fareDetail;
    private List<FareComponent> fareComponent;

    public String getOfferItemID() {
        return offerItemID;
    }

    public void setOfferItemID(String offerItemID) {
        this.offerItemID = offerItemID;
    }

    public String getRefundable() {
        return refundable;
    }

    public void setRefundable(String refundable) {
        this.refundable = refundable;
    }

    public String getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(String passengerType) {
        this.passengerType = passengerType;
    }

    public Integer getPassengerQuantity() {
        return passengerQuantity;
    }

    public void setPassengerQuantity(Integer passengerQuantity) {
        this.passengerQuantity = passengerQuantity;
    }

    public TotalPriceDetail getTotalPriceDetail() {
        return totalPriceDetail;
    }

    public void setTotalPriceDetail(TotalPriceDetail totalPriceDetail) {
        this.totalPriceDetail = totalPriceDetail;
    }

    public List<Service> getService() {
        return service;
    }

    public void setService(List<Service> service) {
        this.service = service;
    }

    public FareDetail getFareDetail() {
        return fareDetail;
    }

    public void setFareDetail(FareDetail fareDetail) {
        this.fareDetail = fareDetail;
    }

    public List<FareComponent> getFareComponent() {
        return fareComponent;
    }

    public void setFareComponent(List<FareComponent> fareComponent) {
        this.fareComponent = fareComponent;
    }
}