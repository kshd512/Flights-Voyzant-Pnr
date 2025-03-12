package com.mmt.flights.entity.pnr.retrieve.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mmt.flights.entity.common.Document;
import com.mmt.flights.entity.common.Party;

public class OrderRetreiveRQ {
    @JsonProperty("Document")
    private Document document;
    
    @JsonProperty("party")
    private Party party;
    
    @JsonProperty("Query")
    private Query query;

    // Getters and Setters
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
}