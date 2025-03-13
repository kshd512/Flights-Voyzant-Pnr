package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceClass {
    @JsonProperty("PriceClassID")
    private String priceClassID;
    
    @JsonProperty("Name")
    private String name;
    
    @JsonProperty("Code")
    private String code;
    
    @JsonProperty("Descriptions")
    private Descriptions descriptions;

    public String getPriceClassID() {
        return priceClassID;
    }

    public void setPriceClassID(String priceClassID) {
        this.priceClassID = priceClassID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Descriptions getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Descriptions descriptions) {
        this.descriptions = descriptions;
    }
}