package com.mmt.flights.common.config;

import com.mmt.flights.hivelogger.entity.HiveProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HivePropertiesProps {

    @Bean(name = "hivePropertiesColumbus")
    @ConfigurationProperties(prefix = "flights.pnr.hive.kafka")
    public HiveProperties hiveProperties() {
        return new HiveProperties();
    }
}
