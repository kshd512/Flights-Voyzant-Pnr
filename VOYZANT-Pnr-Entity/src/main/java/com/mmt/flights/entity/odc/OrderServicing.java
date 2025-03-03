package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderServicing {
    @JsonProperty("Add")
    private Add add;
    
    @JsonProperty("AcceptOffer")
    private AcceptOffer acceptOffer;

    public Add getAdd() {
        return add;
    }

    public void setAdd(Add add) {
        this.add = add;
    }
    
    public AcceptOffer getAcceptOffer() {
        return acceptOffer;
    }

    public void setAcceptOffer(AcceptOffer acceptOffer) {
        this.acceptOffer = acceptOffer;
    }
}