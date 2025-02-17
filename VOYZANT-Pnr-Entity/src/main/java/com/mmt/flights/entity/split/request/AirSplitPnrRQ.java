package com.mmt.flights.entity.split.request;

import lombok.Data;

@Data
public class AirSplitPnrRQ {
    private Document document;
    private Party party;
    private Query query;
    private DataLists dataLists;
}