package com.mmt.flights.odc.service.tasks;



import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "flights.odc.config")
public class ODCConfig {
    private Boolean enableNegativeFare;

    public Boolean getEnableNegativeFare() {
        return enableNegativeFare;
    }

    public void setEnableNegativeFare(Boolean enableNegativeFare) {
        this.enableNegativeFare = enableNegativeFare;
    }
}
