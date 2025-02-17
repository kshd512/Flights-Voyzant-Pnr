package com.mmt.flights.entity.split.response;

import lombok.Data;

@Data
public class AirSplitPnrRS {
    private Document document;
    private Party party;
    private String shoppingResponseId;
    private Success success;
    private String originalOrderID;
    private String splitedOrderID;
    private String originalGdsBookingReference;
    private String splitedGdsBookingReference;
}