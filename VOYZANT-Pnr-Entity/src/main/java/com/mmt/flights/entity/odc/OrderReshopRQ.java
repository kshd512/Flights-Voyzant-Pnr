package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderReshopRQ {
    @JsonProperty("Document")
    private Document document;

    @JsonProperty("Party")
    private Party party;

    @JsonProperty("Query")
    private Query query;
    
    @JsonProperty("ShoppingResponseId")
    private String shoppingResponseId;

    @JsonProperty("DataLists")
    private DataLists dataLists;

    @JsonProperty("Preference")
    private Preference preference;

    @JsonProperty("MetaData")
    private MetaData metaData;

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

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }
    
    public String getShoppingResponseId() {
        return shoppingResponseId;
    }

    public void setShoppingResponseId(String shoppingResponseId) {
        this.shoppingResponseId = shoppingResponseId;
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

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }
}