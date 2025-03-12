
package com.mmt.flights.entity.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identifier",
    "carrierCode",
    "opSuffix"
})
public class Identifier {

    @JsonProperty("identifier")
    private String identifier;
    @JsonProperty("carrierCode")
    private String carrierCode;
    @JsonProperty("opSuffix")
    private Object opSuffix;

    @JsonProperty("identifier")
    public String getIdentifier() {
        return identifier;
    }

    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @JsonProperty("carrierCode")
    public String getCarrierCode() {
        return carrierCode;
    }

    @JsonProperty("carrierCode")
    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    @JsonProperty("opSuffix")
    public Object getOpSuffix() {
        return opSuffix;
    }

    @JsonProperty("opSuffix")
    public void setOpSuffix(Object opSuffix) {
        this.opSuffix = opSuffix;
    }

}
