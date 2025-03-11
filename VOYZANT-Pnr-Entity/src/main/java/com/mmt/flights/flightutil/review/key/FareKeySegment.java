package com.mmt.flights.flightutil.review.key;

public class FareKeySegment {
    private String udf;
    private String psf;
    private String yq;
    private String yr;
    private String fareBasis;
    private String fareClass;
    private String productClass;
    private String pax;

    private FareKeySegment(Builder builder) {
        this.udf = builder.udf;
        this.psf = builder.psf;
        this.yq = builder.yq;
        this.yr = builder.yr;
        this.fareBasis = builder.fareBasis;
        this.fareClass = builder.fareClass;
        this.productClass = builder.productClass;
        this.pax = builder.pax;
    }

    @Override
    public String toString() {
        return String.format("%s|%s|%s|%s|%s|%s|%s|%s", 
            fareBasis != null ? fareBasis : "",
            fareClass != null ? fareClass : "",
            productClass != null ? productClass : "",
            pax != null ? pax : "A",
            udf != null ? udf : "0",
            psf != null ? psf : "0",
            yq != null ? yq : "0",
            yr != null ? yr : "0");
    }

    public static class Builder {
        private String udf;
        private String psf;
        private String yq;
        private String yr;
        private String fareBasis;
        private String fareClass;
        private String productClass;
        private String pax;

        public Builder udf(String udf) {
            this.udf = udf;
            return this;
        }

        public Builder psf(String psf) {
            this.psf = psf;
            return this;
        }

        public Builder yq(String yq) {
            this.yq = yq;
            return this;
        }

        public Builder yr(String yr) {
            this.yr = yr;
            return this;
        }

        public Builder fareBasis(String fareBasis) {
            this.fareBasis = fareBasis;
            return this;
        }

        public Builder fareClass(String fareClass) {
            this.fareClass = fareClass;
            return this;
        }

        public Builder productClass(String productClass) {
            this.productClass = productClass;
            return this;
        }

        public Builder pax(String pax) {
            this.pax = pax;
            return this;
        }

        public FareKeySegment build() {
            return new FareKeySegment(this);
        }
    }
}