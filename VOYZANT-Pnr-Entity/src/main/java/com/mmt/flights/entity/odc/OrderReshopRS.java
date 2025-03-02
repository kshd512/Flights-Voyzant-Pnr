package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderReshopRS {
    @JsonProperty("Document")
    private Document document;

    @JsonProperty("Party")
    private Party party;

    @JsonProperty("ShoppingResponseId")
    private String shoppingResponseId;

    @JsonProperty("Success")
    private Success success;

    @JsonProperty("ReshopOffers")
    private List<ReshopOfferInstance> reshopOffers;

    @JsonProperty("DataLists")
    private DataLists dataLists;

    @JsonProperty("MetaData")
    private MetaData metaData;

    public boolean isSuccess() {
        return success != null;
    }

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

    public Success getSuccess() {
        return success;
    }

    public void setSuccess(Success success) {
        this.success = success;
    }

    public List<ReshopOfferInstance> getReshopOffers() {
        return reshopOffers;
    }

    public void setReshopOffers(List<ReshopOfferInstance> reshopOffers) {
        this.reshopOffers = reshopOffers;
    }

    public DataLists getDataLists() {
        return dataLists;
    }

    public void setDataLists(DataLists dataLists) {
        this.dataLists = dataLists;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }
}