
package com.mmt.flights.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentCode",
    "domainCode",
    "locationCode",
    "organizationCode"
})
public class PointOfSale {

    @JsonProperty("agentCode")
    private String agentCode;
    @JsonProperty("domainCode")
    private String domainCode;
    @JsonProperty("locationCode")
    private String locationCode;
    @JsonProperty("organizationCode")
    private String organizationCode;

    @JsonProperty("agentCode")
    public String getAgentCode() {
        return agentCode;
    }

    @JsonProperty("agentCode")
    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }

    @JsonProperty("domainCode")
    public String getDomainCode() {
        return domainCode;
    }

    @JsonProperty("domainCode")
    public void setDomainCode(String domainCode) {
        this.domainCode = domainCode;
    }

    @JsonProperty("locationCode")
    public String getLocationCode() {
        return locationCode;
    }

    @JsonProperty("locationCode")
    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    @JsonProperty("organizationCode")
    public String getOrganizationCode() {
        return organizationCode;
    }

    @JsonProperty("organizationCode")
    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

}
