package com.mmt.flights.flightutil.review.key;

import java.util.Map;

public class RecommendationKey {
    private Map<Integer, RecommendationKeyFragment> recommendationKeyMap;

    public void setRecommendationKeyMap(Map<Integer, RecommendationKeyFragment> recommendationKeyMap) {
        this.recommendationKeyMap = recommendationKeyMap;
    }

    public String build() {
        StringBuilder key = new StringBuilder();
        recommendationKeyMap.forEach((index, fragment) -> {
            if (index > 0) {
                key.append("~");
            }
            key.append(fragment.build());
        });
        return key.toString();
    }
}