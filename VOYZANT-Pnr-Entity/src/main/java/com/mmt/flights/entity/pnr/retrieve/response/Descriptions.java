package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Descriptions {
    @JsonProperty("Description")
    private List<Object> description;

    public List<Object> getDescription() {
        return description;
    }

    public void setDescription(List<Object> description) {
        this.description = description;
    }
}