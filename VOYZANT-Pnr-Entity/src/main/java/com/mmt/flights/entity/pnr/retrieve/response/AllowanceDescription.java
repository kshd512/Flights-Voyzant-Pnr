package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AllowanceDescription {
    @JsonProperty("ApplicableParty")
    private String applicableParty;
    
    @JsonProperty("Description")
    private String description;

    public String getApplicableParty() {
        return applicableParty;
    }

    public void setApplicableParty(String applicableParty) {
        this.applicableParty = applicableParty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}