package com.mmt.flights.flightutil.review.key;

public class FlightLevelInformation {
    private int adultNumber;
    private int childNumber;
    private int infantNumber;

    private FlightLevelInformation(Builder builder) {
        this.adultNumber = builder.adultNumber;
        this.childNumber = builder.childNumber;
        this.infantNumber = builder.infantNumber;
    }

    @Override
    public String toString() {
        return String.format("%d|%d|%d", adultNumber, childNumber, infantNumber);
    }

    public static class Builder {
        private int adultNumber;
        private int childNumber;
        private int infantNumber;

        public Builder adultNumber(int adultNumber) {
            this.adultNumber = adultNumber;
            return this;
        }

        public Builder childNumber(int childNumber) {
            this.childNumber = childNumber;
            return this;
        }

        public Builder infantNumber(int infantNumber) {
            this.infantNumber = infantNumber;
            return this;
        }

        public FlightLevelInformation build() {
            return new FlightLevelInformation(this);
        }
    }
}