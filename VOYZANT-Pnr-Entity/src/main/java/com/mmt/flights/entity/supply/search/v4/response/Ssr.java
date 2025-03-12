
package com.mmt.flights.entity.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.validation.Valid;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "isConfirmed",
    "isConfirmingUnheld",
    "note",
    "ssrDuration",
    "ssrKey",
    "count",
    "ssrCode",
    "feeCode",
    "inBundle",
    "passengerKey",
    "ssrDetail",
    "ssrNumber",
    "market"
})
public class Ssr {

    @JsonProperty("isConfirmed")
    private Boolean isConfirmed;
    @JsonProperty("isConfirmingUnheld")
    private Boolean isConfirmingUnheld;
    @JsonProperty("note")
    private String note;
    @JsonProperty("ssrDuration")
    private Integer ssrDuration;
    @JsonProperty("ssrKey")
    private String ssrKey;
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("ssrCode")
    private String ssrCode;
    @JsonProperty("feeCode")
    private Object feeCode;
    @JsonProperty("inBundle")
    private Boolean inBundle;
    @JsonProperty("passengerKey")
    private String passengerKey;
    @JsonProperty("ssrDetail")
    private Object ssrDetail;
    @JsonProperty("ssrNumber")
    private Integer ssrNumber;
    @JsonProperty("market")
    @Valid
    private Market market;

    @JsonProperty("isConfirmed")
    public Boolean getIsConfirmed() {
        return isConfirmed;
    }

    @JsonProperty("isConfirmed")
    public void setIsConfirmed(Boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    @JsonProperty("isConfirmingUnheld")
    public Boolean getIsConfirmingUnheld() {
        return isConfirmingUnheld;
    }

    @JsonProperty("isConfirmingUnheld")
    public void setIsConfirmingUnheld(Boolean isConfirmingUnheld) {
        this.isConfirmingUnheld = isConfirmingUnheld;
    }

    @JsonProperty("note")
    public String getNote() {
        return note;
    }

    @JsonProperty("note")
    public void setNote(String note) {
        this.note = note;
    }

    @JsonProperty("ssrDuration")
    public Integer getSsrDuration() {
        return ssrDuration;
    }

    @JsonProperty("ssrDuration")
    public void setSsrDuration(Integer ssrDuration) {
        this.ssrDuration = ssrDuration;
    }

    @JsonProperty("ssrKey")
    public String getSsrKey() {
        return ssrKey;
    }

    @JsonProperty("ssrKey")
    public void setSsrKey(String ssrKey) {
        this.ssrKey = ssrKey;
    }

    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    @JsonProperty("count")
    public void setCount(Integer count) {
        this.count = count;
    }

    @JsonProperty("ssrCode")
    public String getSsrCode() {
        return ssrCode;
    }

    @JsonProperty("ssrCode")
    public void setSsrCode(String ssrCode) {
        this.ssrCode = ssrCode;
    }

    @JsonProperty("feeCode")
    public Object getFeeCode() {
        return feeCode;
    }

    @JsonProperty("feeCode")
    public void setFeeCode(Object feeCode) {
        this.feeCode = feeCode;
    }

    @JsonProperty("inBundle")
    public Boolean getInBundle() {
        return inBundle;
    }

    @JsonProperty("inBundle")
    public void setInBundle(Boolean inBundle) {
        this.inBundle = inBundle;
    }

    @JsonProperty("passengerKey")
    public String getPassengerKey() {
        return passengerKey;
    }

    @JsonProperty("passengerKey")
    public void setPassengerKey(String passengerKey) {
        this.passengerKey = passengerKey;
    }

    @JsonProperty("ssrDetail")
    public Object getSsrDetail() {
        return ssrDetail;
    }

    @JsonProperty("ssrDetail")
    public void setSsrDetail(Object ssrDetail) {
        this.ssrDetail = ssrDetail;
    }

    @JsonProperty("ssrNumber")
    public Integer getSsrNumber() {
        return ssrNumber;
    }

    @JsonProperty("ssrNumber")
    public void setSsrNumber(Integer ssrNumber) {
        this.ssrNumber = ssrNumber;
    }

    @JsonProperty("market")
    public Market getMarket() {
        return market;
    }

    @JsonProperty("market")
    public void setMarket(Market market) {
        this.market = market;
    }

}
