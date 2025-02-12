package com.mmt.flights.entity.pnr.retrieve.response;

import java.util.List;
import java.util.Map;

public class RewardSettings {
    private String rewardAvailable;
    private List<Object> pointTypes;
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