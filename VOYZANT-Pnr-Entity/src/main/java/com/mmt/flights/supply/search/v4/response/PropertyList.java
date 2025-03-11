
package com.mmt.flights.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "SRVZONE",
    "BRDZONE",
    "LEGROOM",
    "BULKHEAD",
    "EXITROW",
    "LAVATORY",
    "TCC"
})
public class PropertyList {

    @JsonProperty("SRVZONE")
    private String sRVZONE;
    @JsonProperty("BRDZONE")
    private String bRDZONE;
    @JsonProperty("LEGROOM")
    private String lEGROOM;
    @JsonProperty("BULKHEAD")
    private String bULKHEAD;
    @JsonProperty("EXITROW")
    private String eXITROW;
    @JsonProperty("LAVATORY")
    private String lAVATORY;
    @JsonProperty("TCC")
    private String tCC;

    @JsonProperty("SRVZONE")
    public String getSRVZONE() {
        return sRVZONE;
    }

    @JsonProperty("SRVZONE")
    public void setSRVZONE(String sRVZONE) {
        this.sRVZONE = sRVZONE;
    }

    @JsonProperty("BRDZONE")
    public String getBRDZONE() {
        return bRDZONE;
    }

    @JsonProperty("BRDZONE")
    public void setBRDZONE(String bRDZONE) {
        this.bRDZONE = bRDZONE;
    }

    @JsonProperty("LEGROOM")
    public String getLEGROOM() {
        return lEGROOM;
    }

    @JsonProperty("LEGROOM")
    public void setLEGROOM(String lEGROOM) {
        this.lEGROOM = lEGROOM;
    }

    @JsonProperty("BULKHEAD")
    public String getBULKHEAD() {
        return bULKHEAD;
    }

    @JsonProperty("BULKHEAD")
    public void setBULKHEAD(String bULKHEAD) {
        this.bULKHEAD = bULKHEAD;
    }

    @JsonProperty("EXITROW")
    public String getEXITROW() {
        return eXITROW;
    }

    @JsonProperty("EXITROW")
    public void setEXITROW(String eXITROW) {
        this.eXITROW = eXITROW;
    }

    @JsonProperty("LAVATORY")
    public String getLAVATORY() {
        return lAVATORY;
    }

    @JsonProperty("LAVATORY")
    public void setLAVATORY(String lAVATORY) {
        this.lAVATORY = lAVATORY;
    }

    @JsonProperty("TCC")
    public String getTCC() {
        return tCC;
    }

    @JsonProperty("TCC")
    public void setTCC(String tCC) {
        this.tCC = tCC;
    }

}
