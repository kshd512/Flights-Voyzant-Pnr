
package com.mmt.flights.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "amount",
    "code",
    "detail",
    "type",
    "collectType",
    "currencyCode",
    "foreignCurrencyCode",
    "foreignAmount",
    "ticketCode"
})
public class ServiceCharge {

    @JsonProperty("amount")
    private double amount;
    @JsonProperty("code")
    private String code;
    @JsonProperty("detail")
    private String detail;
    @JsonProperty("type")
    private ServiceChargeType type;
    @JsonProperty("collectType")
    private Integer collectType;
    @JsonProperty("currencyCode")
    private String currencyCode;
    @JsonProperty("foreignCurrencyCode")
    private String foreignCurrencyCode;
    @JsonProperty("foreignAmount")
    private Integer foreignAmount;
    @JsonProperty("ticketCode")
    private String ticketCode;

    @JsonProperty("amount")
    public double getAmount() {
        return amount;
    }

    @JsonProperty("amount")
    public void setAmount(double amount) {
        this.amount = amount;
    }

    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("detail")
    public String getDetail() {
        return detail;
    }

    @JsonProperty("detail")
    public void setDetail(String detail) {
        this.detail = detail;
    }

    @JsonProperty("type")
    public ServiceChargeType getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(ServiceChargeType type) {
        this.type = type;
    }

    @JsonProperty("collectType")
    public Integer getCollectType() {
        return collectType;
    }

    @JsonProperty("collectType")
    public void setCollectType(Integer collectType) {
        this.collectType = collectType;
    }

    @JsonProperty("currencyCode")
    public String getCurrencyCode() {
        return currencyCode;
    }

    @JsonProperty("currencyCode")
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @JsonProperty("foreignCurrencyCode")
    public String getForeignCurrencyCode() {
        return foreignCurrencyCode;
    }

    @JsonProperty("foreignCurrencyCode")
    public void setForeignCurrencyCode(String foreignCurrencyCode) {
        this.foreignCurrencyCode = foreignCurrencyCode;
    }

    @JsonProperty("foreignAmount")
    public Integer getForeignAmount() {
        return foreignAmount;
    }

    @JsonProperty("foreignAmount")
    public void setForeignAmount(Integer foreignAmount) {
        this.foreignAmount = foreignAmount;
    }

    @JsonProperty("ticketCode")
    public String getTicketCode() {
        return ticketCode;
    }

    @JsonProperty("ticketCode")
    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

}
