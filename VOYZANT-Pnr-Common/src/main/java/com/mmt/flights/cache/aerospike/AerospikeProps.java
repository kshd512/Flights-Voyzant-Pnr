package com.mmt.flights.cache.aerospike;

public class AerospikeProps {
    private String namespace;
    private String set;
    private String hostAndPort;
    private String secretName;
    private int readPolicySocketTimeout;
    private int readPolicyTotalTimeout;
    private int readPolicySleepBetweenRetries;
    private int writePolicySocketTimeout;
    private int writePolicyTotalTimeout;
    private int writePolicySleepBetweenRetries;
    private int expiryInSeconds;
    private boolean fallbackEnabled;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getHostAndPort() {
        return hostAndPort;
    }

    public void setHostAndPort(String hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public int getReadPolicySocketTimeout() {
        return readPolicySocketTimeout;
    }

    public void setReadPolicySocketTimeout(int readPolicySocketTimeout) {
        this.readPolicySocketTimeout = readPolicySocketTimeout;
    }

    public int getReadPolicyTotalTimeout() {
        return readPolicyTotalTimeout;
    }

    public void setReadPolicyTotalTimeout(int readPolicyTotalTimeout) {
        this.readPolicyTotalTimeout = readPolicyTotalTimeout;
    }

    public int getReadPolicySleepBetweenRetries() {
        return readPolicySleepBetweenRetries;
    }

    public void setReadPolicySleepBetweenRetries(int readPolicySleepBetweenRetries) {
        this.readPolicySleepBetweenRetries = readPolicySleepBetweenRetries;
    }

    public int getWritePolicySocketTimeout() {
        return writePolicySocketTimeout;
    }

    public void setWritePolicySocketTimeout(int writePolicySocketTimeout) {
        this.writePolicySocketTimeout = writePolicySocketTimeout;
    }

    public int getWritePolicyTotalTimeout() {
        return writePolicyTotalTimeout;
    }

    public void setWritePolicyTotalTimeout(int writePolicyTotalTimeout) {
        this.writePolicyTotalTimeout = writePolicyTotalTimeout;
    }

    public int getWritePolicySleepBetweenRetries() {
        return writePolicySleepBetweenRetries;
    }

    public void setWritePolicySleepBetweenRetries(int writePolicySleepBetweenRetries) {
        this.writePolicySleepBetweenRetries = writePolicySleepBetweenRetries;
    }

    public int getExpiryInSeconds() {
        return expiryInSeconds;
    }

    public void setExpiryInSeconds(int expiryInSeconds) {
        this.expiryInSeconds = expiryInSeconds;
    }

    public boolean isFallbackEnabled() {
        return fallbackEnabled;
    }

    public void setFallbackEnabled(boolean fallbackEnabled) {
        this.fallbackEnabled = fallbackEnabled;
    }

    @Override
    public String toString() {
        return "AerospikeProps [namespace=" + namespace + ", set=" + set + ", hostAndPort=" + hostAndPort
                + ", readPolicySocketTimeout=" + readPolicySocketTimeout + ", readPolicyTotalTimeout="
                + readPolicyTotalTimeout + ", readPolicySleepBetweenRetries=" + readPolicySleepBetweenRetries
                + ", writePolicySocketTimeout=" + writePolicySocketTimeout + ", writePolicyTotalTimeout="
                + writePolicyTotalTimeout + ", writePolicySleepBetweenRetries=" + writePolicySleepBetweenRetries
                + ", expiryInSeconds=" + expiryInSeconds + ", fallbackEnabled=" + fallbackEnabled + "]";
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }
}
