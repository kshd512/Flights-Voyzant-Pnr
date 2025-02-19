package com.mmt.flights.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;


@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "flights.voyzant.config")
public class VOYZANTConfig {
}

