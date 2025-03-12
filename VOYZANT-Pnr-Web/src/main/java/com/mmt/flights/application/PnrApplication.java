package com.mmt.flights.application;

import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;
import com.mmt.flights.constants.Constants;
import com.mmt.flights.flightsutil.AirportDetailsUtil;
import com.mmt.flights.helper.CommonCurrencyConverter;
import com.mmt.flights.helper.CurrencyConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;

/**
 * Application startup file which handles base package scanner and
 * loading profile specific yaml file.Profile is controlled by provided
 * profile as system variable on starting the application with values
 * dev or prod.
 */
@SpringBootApplication
@EnableHystrix
@EnableHystrixDashboard
@ComponentScan(basePackages = {Constants.SPRING_PACKAGE_SCANNER})
public class PnrApplication {

    private static final Logger logger = LoggerFactory.getLogger(PnrApplication.class);

    public static void main(String[] args) {
        try {
            SpringApplication application = new SpringApplication(PnrApplication.class);
            application.setEnvironment(loadProfile());
            application.run(args);
            System.out.println("Server is running on port 8081");
            logger.info("Server is running on port 8081");
        } catch (Exception e) {
            logger.error("Unexpected error during application startup: " + e.getMessage(), e);
            System.out.println("CRITICAL ERROR: Unexpected error during application startup");
        }
    }

    public static ConfigurableEnvironment loadProfile() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        if (StringUtils.isEmpty(System.getProperty(Constants.PROFILE)))
            throw new IllegalArgumentException("Profile not Specified.");
        environment.setActiveProfiles(System.getProperty(Constants.PROFILE));
        return environment;
    }

    @Bean
    @Primary
    ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        Printer defaultValuesPrinter = JsonFormat.printer().includingDefaultValueFields();
        return new ProtobufJsonFormatHttpMessageConverter(JsonFormat.parser(), defaultValuesPrinter);
    }

    @Bean
    RestTemplate restTemplate(ProtobufHttpMessageConverter hmc) {
        return new RestTemplate(Arrays.asList(hmc));
    }

    @Bean
    public CurrencyConverter getCurrencyConverter() {
        return new CommonCurrencyConverter();
    }

    @Bean
    AirportDetailsUtil getAirportDetailsUtil() throws IOException {
        return AirportDetailsUtil.getInstance();
    }

}
