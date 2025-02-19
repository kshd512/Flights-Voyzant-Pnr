package com.mmt.flights.entity.split.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
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