package com.mmt.flights.cache.aerospike;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "flights.cache.aerospike-config")
public class AerospikeBaseConfig extends AerospikeProps {

    private int cancelDataExpiryInSeconds;
    private int saveRetry;

    public int getCancelDataExpiryInSeconds() {
        return cancelDataExpiryInSeconds;
    }

    public void setCancelDataExpiryInSeconds(int cancelDataExpiryInSeconds) {
        this.cancelDataExpiryInSeconds = cancelDataExpiryInSeconds;
    }

    public int getSaveRetry() {
        return saveRetry;
    }

    public void setSaveRetry(int saveRetry) {
        this.saveRetry = saveRetry;
    }
}
