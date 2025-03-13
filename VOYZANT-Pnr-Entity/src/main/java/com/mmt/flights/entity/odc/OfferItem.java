package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfferItem {
    @JsonProperty("OfferItemID")
    private String offerItemID;

    @JsonProperty("Refundable")
    private String refundable;

    @JsonProperty("PassengerType")
    private String passengerType;

    @JsonProperty("PassengerQuantity")
    private int passengerQuantity;

    @JsonProperty("TotalPriceDetail")
    private TotalPriceDetail totalPriceDetail;

    @JsonProperty("Service")
    private List<Service> service;

    @JsonProperty("FareDetail")
    private FareDetail fareDetail;

    @JsonProperty("FareComponent")
    private List<FareComponent> fareComponent;

    // Getters and setters
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

    public int getPassengerQuantity() {
        return passengerQuantity;
    }

    public void setPassengerQuantity(int passengerQuantity) {
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