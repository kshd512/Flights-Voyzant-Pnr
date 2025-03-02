package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Qualifier {
    @JsonProperty("ExistingOrderQualifier")
    private ExistingOrderQualifier existingOrderQualifier;

    public ExistingOrderQualifier getExistingOrderQualifier() {
        return existingOrderQualifier;
    }

    public void setExistingOrderQualifier(ExistingOrderQualifier existingOrderQualifier) {
        this.existingOrderQualifier = existingOrderQualifier;
    }
}