package com.mmt.flights.entity.cancel.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mmt.flights.entity.cancel.common.Document;
import com.mmt.flights.entity.cancel.common.Party;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketVoidRS {

    @JsonProperty("Document")
    private Document document;

    @JsonProperty("Party")
    private Party party;

    @JsonProperty("ShoppingResponseId")
    private String shoppingResponseId;

    @JsonProperty("Success")
    private Success success;

    @JsonProperty("Result")
    private Result result;

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

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
