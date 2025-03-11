
package com.mmt.flights.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.validation.Valid;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "isGoverning",
    "downgradeAvailable",
    "carrierCode",
    "fareKey",
    "classOfService",
    "classType",
    "fareApplicationType",
    "fareClassOfService",
    "fareBasisCode",
    "fareSequence",
    "inboundOutBound",
    "fareStatus",
    "isAllotmentMarketFare",
    "originalClassOfService",
    "ruleNumber",
    "productClass",
    "ruleTariff",
    "travelClassCode",
    "crossReferenceClassOfService",
    "passengerFares",
    "fareLink"
})
public class Fare {

    @JsonProperty("isGoverning")
    private Boolean isGoverning;
    @JsonProperty("downgradeAvailable")
    private Boolean downgradeAvailable;
    @JsonProperty("carrierCode")
    private String carrierCode;
    @JsonProperty("fareKey")
    private String fareKey;
    @JsonProperty("classOfService")
    private String classOfService;
    @JsonProperty("classType")
    private Object classType;
    @JsonProperty("fareApplicationType")
    private FareApplicationType fareApplicationType;
    @JsonProperty("fareClassOfService")
    private String fareClassOfService;
    @JsonProperty("fareBasisCode")
    private String fareBasisCode;
    @JsonProperty("fareSequence")
    private Integer fareSequence;
    @JsonProperty("inboundOutBound")
    private Integer inboundOutBound;
    @JsonProperty("fareStatus")
    private Integer fareStatus;
    @JsonProperty("isAllotmentMarketFare")
    private Boolean isAllotmentMarketFare;
    @JsonProperty("originalClassOfService")
    private String originalClassOfService;
    @JsonProperty("ruleNumber")
    private String ruleNumber;
    @JsonProperty("productClass")
    private String productClass;
    @JsonProperty("ruleTariff")
    private Object ruleTariff;
    @JsonProperty("travelClassCode")
    private String travelClassCode;
    @JsonProperty("crossReferenceClassOfService")
    private Object crossReferenceClassOfService;
    @JsonProperty("passengerFares")
    @Valid
    private List<PassengerFare> passengerFares = null;
    @JsonProperty("fareLink")
    private Integer fareLink;

    @JsonProperty("isGoverning")
    public Boolean getIsGoverning() {
        return isGoverning;
    }

    @JsonProperty("isGoverning")
    public void setIsGoverning(Boolean isGoverning) {
        this.isGoverning = isGoverning;
    }

    @JsonProperty("downgradeAvailable")
    public Boolean getDowngradeAvailable() {
        return downgradeAvailable;
    }

    @JsonProperty("downgradeAvailable")
    public void setDowngradeAvailable(Boolean downgradeAvailable) {
        this.downgradeAvailable = downgradeAvailable;
    }

    @JsonProperty("carrierCode")
    public String getCarrierCode() {
        return carrierCode;
    }

    @JsonProperty("carrierCode")
    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    @JsonProperty("fareKey")
    public String getFareKey() {
        return fareKey;
    }

    @JsonProperty("fareKey")
    public void setFareKey(String fareKey) {
        this.fareKey = fareKey;
    }

    @JsonProperty("classOfService")
    public String getClassOfService() {
        return classOfService;
    }

    @JsonProperty("classOfService")
    public void setClassOfService(String classOfService) {
        this.classOfService = classOfService;
    }

    @JsonProperty("classType")
    public Object getClassType() {
        return classType;
    }

    @JsonProperty("classType")
    public void setClassType(Object classType) {
        this.classType = classType;
    }

    @JsonProperty("fareApplicationType")
    public FareApplicationType getFareApplicationType() {
        return fareApplicationType;
    }

    @JsonProperty("fareApplicationType")
    public void setFareApplicationType(FareApplicationType fareApplicationType) {
        this.fareApplicationType = fareApplicationType;
    }

    @JsonProperty("fareClassOfService")
    public String getFareClassOfService() {
        return fareClassOfService;
    }

    @JsonProperty("fareClassOfService")
    public void setFareClassOfService(String fareClassOfService) {
        this.fareClassOfService = fareClassOfService;
    }

    @JsonProperty("fareBasisCode")
    public String getFareBasisCode() {
        return fareBasisCode;
    }

    @JsonProperty("fareBasisCode")
    public void setFareBasisCode(String fareBasisCode) {
        this.fareBasisCode = fareBasisCode;
    }

    @JsonProperty("fareSequence")
    public Integer getFareSequence() {
        return fareSequence;
    }

    @JsonProperty("fareSequence")
    public void setFareSequence(Integer fareSequence) {
        this.fareSequence = fareSequence;
    }

    @JsonProperty("inboundOutBound")
    public Integer getInboundOutBound() {
        return inboundOutBound;
    }

    @JsonProperty("inboundOutBound")
    public void setInboundOutBound(Integer inboundOutBound) {
        this.inboundOutBound = inboundOutBound;
    }

    @JsonProperty("fareStatus")
    public Integer getFareStatus() {
        return fareStatus;
    }

    @JsonProperty("fareStatus")
    public void setFareStatus(Integer fareStatus) {
        this.fareStatus = fareStatus;
    }

    @JsonProperty("isAllotmentMarketFare")
    public Boolean getIsAllotmentMarketFare() {
        return isAllotmentMarketFare;
    }

    @JsonProperty("isAllotmentMarketFare")
    public void setIsAllotmentMarketFare(Boolean isAllotmentMarketFare) {
        this.isAllotmentMarketFare = isAllotmentMarketFare;
    }

    @JsonProperty("originalClassOfService")
    public String getOriginalClassOfService() {
        return originalClassOfService;
    }

    @JsonProperty("originalClassOfService")
    public void setOriginalClassOfService(String originalClassOfService) {
        this.originalClassOfService = originalClassOfService;
    }

    @JsonProperty("ruleNumber")
    public String getRuleNumber() {
        return ruleNumber;
    }

    @JsonProperty("ruleNumber")
    public void setRuleNumber(String ruleNumber) {
        this.ruleNumber = ruleNumber;
    }

    @JsonProperty("productClass")
    public String getProductClass() {
        return productClass;
    }

    @JsonProperty("productClass")
    public void setProductClass(String productClass) {
        this.productClass = productClass;
    }

    @JsonProperty("ruleTariff")
    public Object getRuleTariff() {
        return ruleTariff;
    }

    @JsonProperty("ruleTariff")
    public void setRuleTariff(Object ruleTariff) {
        this.ruleTariff = ruleTariff;
    }

    @JsonProperty("travelClassCode")
    public String getTravelClassCode() {
        return travelClassCode;
    }

    @JsonProperty("travelClassCode")
    public void setTravelClassCode(String travelClassCode) {
        this.travelClassCode = travelClassCode;
    }

    @JsonProperty("crossReferenceClassOfService")
    public Object getCrossReferenceClassOfService() {
        return crossReferenceClassOfService;
    }

    @JsonProperty("crossReferenceClassOfService")
    public void setCrossReferenceClassOfService(Object crossReferenceClassOfService) {
        this.crossReferenceClassOfService = crossReferenceClassOfService;
    }

    @JsonProperty("passengerFares")
    public List<PassengerFare> getPassengerFares() {
        return passengerFares;
    }

    @JsonProperty("passengerFares")
    public void setPassengerFares(List<PassengerFare> passengerFares) {
        this.passengerFares = passengerFares;
    }

    @JsonProperty("fareLink")
    public Integer getFareLink() {
        return fareLink;
    }

    @JsonProperty("fareLink")
    public void setFareLink(Integer fareLink) {
        this.fareLink = fareLink;
    }

}
