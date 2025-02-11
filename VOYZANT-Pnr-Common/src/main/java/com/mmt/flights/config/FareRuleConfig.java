package com.mmt.flights.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "flights.pnr.fare-rules")
public class FareRuleConfig {

    private String fareRuleUrl;

    public String getFareRuleUrl() {
        return fareRuleUrl;
    }

    public void setFareRuleUrl(String fareRuleUrl) {
        this.fareRuleUrl = fareRuleUrl;
    }
}
