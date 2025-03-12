package com.mmt.flights.entity.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Party {
    @JsonProperty("Sender")
    private Sender sender;

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }
}