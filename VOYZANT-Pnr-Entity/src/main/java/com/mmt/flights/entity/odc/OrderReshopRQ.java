package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mmt.flights.entity.common.Document;
import com.mmt.flights.entity.common.Party;
import com.mmt.flights.entity.pnr.retrieve.response.Payments;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderReshopRQ {
    @JsonProperty("Document")
    private Document document;

    @JsonProperty("Party")
    private Party party;
    
    @JsonProperty("ShoppingResponseId")
    private String shoppingResponseId;
    
    @JsonProperty("OfferResponseId")
    private String offerResponseId;
    
    @JsonProperty("MetaData")
    private MetaData metaData;
    
    @JsonProperty("Query")
    private Query query;
    
    @JsonProperty("BookingType")
    private String bookingType;
    
    @JsonProperty("Payments")
    private Payments payments;
    
    @JsonProperty("DataLists")
    private DataLists dataLists;

    @JsonProperty("Preference")
    private Preference preference;

    // Getters and setters
    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public String getShoppingResponseId() {
        return shoppingResponseId;
    }

    public void setShoppingResponseId(String shoppingResponseId) {
        this.shoppingResponseId = shoppingResponseId;
    }

    public String getOfferResponseId() {
        return offerResponseId;
    }

    public void setOfferResponseId(String offerResponseId) {
        this.offerResponseId = offerResponseId;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public Payments getPayments() {
        return payments;
    }

    public void setPayments(Payments payments) {
        this.payments = payments;
    }

    public DataLists getDataLists() {
        return dataLists;
    }

    public void setDataLists(DataLists dataLists) {
        this.dataLists = dataLists;
    }

    public Preference getPreference() {
        return preference;
    }

    public void setPreference(Preference preference) {
        this.preference = preference;
    }
}