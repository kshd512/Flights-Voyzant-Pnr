package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
class PaymentMethod {
    @JsonProperty("Allowed")
    private String allowed;
    @JsonProperty("Types")
    private Map<String, PaymentType> types;

    public String getAllowed() {
        return allowed;
    }

    public void setAllowed(String allowed) {
        this.allowed = allowed;
    }

    public Map<String, PaymentType> getTypes() {
        return types;
    }

    public void setTypes(Map<String, PaymentType> types) {
        this.types = types;
    }
}