package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Code {
    @JsonProperty("MarriageGroup")
    private String marriageGroup;

    public String getMarriageGroup() {
        return marriageGroup;
    }

    public void setMarriageGroup(String marriageGroup) {
        this.marriageGroup = marriageGroup;
    }
}