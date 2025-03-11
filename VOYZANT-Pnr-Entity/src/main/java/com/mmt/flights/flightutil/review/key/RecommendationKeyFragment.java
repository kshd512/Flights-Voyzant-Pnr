package com.mmt.flights.flightutil.review.key;

import java.util.List;

public class RecommendationKeyFragment {
    private List<RecommKeySegment> segments;
    private RecommKeyOtherInfo otherInfo;

    private RecommendationKeyFragment(Builder builder) {
        this.segments = builder.segments;
        this.otherInfo = builder.otherInfo;
    }

    public String build() {
        StringBuilder key = new StringBuilder();
        segments.forEach(segment -> key.append(segment.build()));
        key.append(otherInfo.build());
        return key.toString();
    }

    public static class Builder {
        private List<RecommKeySegment> segments;
        private RecommKeyOtherInfo otherInfo;

        public Builder segments(List<RecommKeySegment> segments) {
            this.segments = segments;
            return this;
        }

        public Builder otherInfo(RecommKeyOtherInfo otherInfo) {
            this.otherInfo = otherInfo;
            return this;
        }

        public RecommendationKeyFragment build() {
            return new RecommendationKeyFragment(this);
        }
    }
}