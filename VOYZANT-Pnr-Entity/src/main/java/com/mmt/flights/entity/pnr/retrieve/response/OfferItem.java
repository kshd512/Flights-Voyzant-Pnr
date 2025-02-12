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
}