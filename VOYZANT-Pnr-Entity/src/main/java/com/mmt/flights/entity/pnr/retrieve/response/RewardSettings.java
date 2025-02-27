package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RewardSettings {
    @JsonProperty("RewardAvailable")
    private String rewardAvailable;
    @JsonProperty("PointTypes")
    private List<Object> pointTypes;
    @JsonProperty("PointValues")
    private Map<String, Object> pointValues;

    public String getRewardAvailable() {
        return rewardAvailable;
    }

    public void setRewardAvailable(String rewardAvailable) {
        this.rewardAvailable = rewardAvailable;
    }

    public List<Object> getPointTypes() {
        return pointTypes;
    }

    public void setPointTypes(List<Object> pointTypes) {
        this.pointTypes = pointTypes;
    }

    public Map<String, Object> getPointValues() {
        return pointValues;
    }

    public void setPointValues(Map<String, Object> pointValues) {
        this.pointValues = pointValues;
    }
}