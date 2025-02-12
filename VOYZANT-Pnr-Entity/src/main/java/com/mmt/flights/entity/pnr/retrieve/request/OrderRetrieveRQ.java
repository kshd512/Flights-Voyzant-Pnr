package com.mmt.flights.entity.pnr.retrieve.request;

class OrderRetrieveRQ {
    private Document Document;
    private Party party;
    private Query Query;

    // Getters and Setters
    public Document getDocument() {
        return Document;
    }

    public void setDocument(Document document) {
        this.Document = document;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public Query getQuery() {
        return Query;
    }

    public void setQuery(Query query) {
        this.Query = query;
    }
}