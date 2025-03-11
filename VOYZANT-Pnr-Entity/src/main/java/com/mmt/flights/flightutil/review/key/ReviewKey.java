package com.mmt.flights.flightutil.review.key;

public class ReviewKey {
    private int version;
    private String fareKey;
    private String recommendationKey;

    private ReviewKey(Builder builder) {
        this.version = builder.version;
        this.fareKey = builder.fareKey;
        this.recommendationKey = builder.recommendationKey;
    }

    public static class Builder {
        private int version;
        private String fareKey;
        private String recommendationKey;

        public Builder ver(int version) {
            this.version = version;
            return this;
        }

        public Builder fareKey(String fareKey) {
            this.fareKey = fareKey;
            return this;
        }

        public Builder recommendationKey(String recommendationKey) {
            this.recommendationKey = recommendationKey;
            return this;
        }

        public String build() {
            ReviewKey key = new ReviewKey(this);
            return "v" + key.version + "~~" + key.fareKey + "~" + key.recommendationKey;
        }
    }
}