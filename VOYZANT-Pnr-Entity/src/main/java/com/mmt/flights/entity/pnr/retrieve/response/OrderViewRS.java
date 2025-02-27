package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderViewRS {
    @JsonProperty("Document")
    private Document document;
    @JsonProperty("Party")
    private Party party;
    @JsonProperty("ShoppingResponseId")
    private String shoppingResponseId;
    @JsonProperty("Success")
    private Success success;
    @JsonProperty("Payments")
    private Payments payments;
    @JsonProperty("Order")
    private List<Order> order;
    @JsonProperty("DataLists")
    private DataLists dataLists;
    @JsonProperty("MetaData")
    private Map<String, Object> metaData;

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

    public Success getSuccess() {
        return success;
    }

    public void setSuccess(Success success) {
        this.success = success;
    }

    public Payments getPayments() {
        return payments;
    }

    public void setPayments(Payments payments) {
        this.payments = payments;
    }

    public List<Order> getOrder() {
        return order;
    }

    public void setOrder(List<Order> order) {
        this.order = order;
    }

    public DataLists getDataLists() {
        return dataLists;
    }

    public void setDataLists(DataLists dataLists) {
        this.dataLists = dataLists;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }
}