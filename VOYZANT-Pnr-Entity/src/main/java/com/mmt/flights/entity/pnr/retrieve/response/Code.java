package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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