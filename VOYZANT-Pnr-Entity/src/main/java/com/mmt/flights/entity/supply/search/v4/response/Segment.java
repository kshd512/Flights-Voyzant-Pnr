
package com.mmt.flights.entity.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public class Segment {

    @JsonProperty("isStandby")
    private Boolean isStandby;
    @JsonProperty("isConfirming")
    private Boolean isConfirming;
    @JsonProperty("isConfirmingExternal")
    private Boolean isConfirmingExternal;
    @JsonProperty("isBlocked")
    private Boolean isBlocked;
    @JsonProperty("isHosted")
    private Boolean isHosted;
    @JsonProperty("isChangeOfGauge")
    private Boolean isChangeOfGauge;
    @JsonProperty("designator")
    @Valid
    private Designator designator;
    @JsonProperty("isSeatmapViewable")
    private Boolean isSeatmapViewable;
    @JsonProperty("fares")
    @Valid
    private List<Fare> fares = null;
    @JsonProperty("segmentKey")
    private String segmentKey;
    @JsonProperty("identifier")
    @Valid
    private Identifier identifier;
    @JsonProperty("passengerSegment")
    @Valid
    private Map<String,PassengerSegment> passengerSegment;
    @JsonProperty("channelType")
    private Integer channelType;
    @JsonProperty("cabinOfService")
    private String cabinOfService;
    @JsonProperty("externalIdentifier")
    private Object externalIdentifier;
    @JsonProperty("priorityCode")
    private Object priorityCode;
    @JsonProperty("changeReasonCode")
    private Integer changeReasonCode;
    @JsonProperty("segmentType")
    private Integer segmentType;
    @JsonProperty("salesDate")
    private String salesDate;
    @JsonProperty("international")
    private Boolean international;
    @JsonProperty("flightReference")
    private String flightReference;
    @JsonProperty("legs")
    @Valid
    private List<Leg> legs = null;

    @JsonProperty("isStandby")
    public Boolean getIsStandby() {
        return isStandby;
    }

    @JsonProperty("isStandby")
    public void setIsStandby(Boolean isStandby) {
        this.isStandby = isStandby;
    }

    @JsonProperty("isConfirming")
    public Boolean getIsConfirming() {
        return isConfirming;
    }

    @JsonProperty("isConfirming")
    public void setIsConfirming(Boolean isConfirming) {
        this.isConfirming = isConfirming;
    }

    @JsonProperty("isConfirmingExternal")
    public Boolean getIsConfirmingExternal() {
        return isConfirmingExternal;
    }

    @JsonProperty("isConfirmingExternal")
    public void setIsConfirmingExternal(Boolean isConfirmingExternal) {
        this.isConfirmingExternal = isConfirmingExternal;
    }

    @JsonProperty("isBlocked")
    public Boolean getIsBlocked() {
        return isBlocked;
    }

    @JsonProperty("isBlocked")
    public void setIsBlocked(Boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    @JsonProperty("isHosted")
    public Boolean getIsHosted() {
        return isHosted;
    }

    @JsonProperty("isHosted")
    public void setIsHosted(Boolean isHosted) {
        this.isHosted = isHosted;
    }

    @JsonProperty("isChangeOfGauge")
    public Boolean getIsChangeOfGauge() {
        return isChangeOfGauge;
    }

    @JsonProperty("isChangeOfGauge")
    public void setIsChangeOfGauge(Boolean isChangeOfGauge) {
        this.isChangeOfGauge = isChangeOfGauge;
    }

    @JsonProperty("designator")
    public Designator getDesignator() {
        return designator;
    }

    @JsonProperty("designator")
    public void setDesignator(Designator designator) {
        this.designator = designator;
    }

    @JsonProperty("isSeatmapViewable")
    public Boolean getIsSeatmapViewable() {
        return isSeatmapViewable;
    }

    @JsonProperty("isSeatmapViewable")
    public void setIsSeatmapViewable(Boolean isSeatmapViewable) {
        this.isSeatmapViewable = isSeatmapViewable;
    }

    @JsonProperty("fares")
    public List<Fare> getFares() {
        return fares;
    }

    @JsonProperty("fares")
    public void setFares(List<Fare> fares) {
        this.fares = fares;
    }

    @JsonProperty("segmentKey")
    public String getSegmentKey() {
        return segmentKey;
    }

    @JsonProperty("segmentKey")
    public void setSegmentKey(String segmentKey) {
        this.segmentKey = segmentKey;
    }

    @JsonProperty("identifier")
    public Identifier getIdentifier() {
        return identifier;
    }

    @JsonProperty("identifier")
    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    @JsonProperty("passengerSegment")
    public Map<String,PassengerSegment> getPassengerSegment() {
        return passengerSegment;
    }

    @JsonProperty("passengerSegment")
    public void setPassengerSegment(Map<String,PassengerSegment> passengerSegment) {
        this.passengerSegment = passengerSegment;
    }

    @JsonProperty("channelType")
    public Integer getChannelType() {
        return channelType;
    }

    @JsonProperty("channelType")
    public void setChannelType(Integer channelType) {
        this.channelType = channelType;
    }

    @JsonProperty("cabinOfService")
    public String getCabinOfService() {
        return cabinOfService;
    }

    @JsonProperty("cabinOfService")
    public void setCabinOfService(String cabinOfService) {
        this.cabinOfService = cabinOfService;
    }

    @JsonProperty("externalIdentifier")
    public Object getExternalIdentifier() {
        return externalIdentifier;
    }

    @JsonProperty("externalIdentifier")
    public void setExternalIdentifier(Object externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
    }

    @JsonProperty("priorityCode")
    public Object getPriorityCode() {
        return priorityCode;
    }

    @JsonProperty("priorityCode")
    public void setPriorityCode(Object priorityCode) {
        this.priorityCode = priorityCode;
    }

    @JsonProperty("changeReasonCode")
    public Integer getChangeReasonCode() {
        return changeReasonCode;
    }

    @JsonProperty("changeReasonCode")
    public void setChangeReasonCode(Integer changeReasonCode) {
        this.changeReasonCode = changeReasonCode;
    }

    @JsonProperty("segmentType")
    public Integer getSegmentType() {
        return segmentType;
    }

    @JsonProperty("segmentType")
    public void setSegmentType(Integer segmentType) {
        this.segmentType = segmentType;
    }

    @JsonProperty("salesDate")
    public String getSalesDate() {
        return salesDate;
    }

    @JsonProperty("salesDate")
    public void setSalesDate(String salesDate) {
        this.salesDate = salesDate;
    }

    @JsonProperty("international")
    public Boolean getInternational() {
        return international;
    }

    @JsonProperty("international")
    public void setInternational(Boolean international) {
        this.international = international;
    }

    @JsonProperty("flightReference")
    public String getFlightReference() {
        return flightReference;
    }

    @JsonProperty("flightReference")
    public void setFlightReference(String flightReference) {
        this.flightReference = flightReference;
    }

    @JsonProperty("legs")
    public List<Leg> getLegs() {
        return legs;
    }

    @JsonProperty("legs")
    public void setLegs(List<Leg> legs) {
        this.legs = legs;
    }

}
