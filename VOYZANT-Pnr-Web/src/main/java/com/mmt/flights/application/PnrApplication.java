package com.mmt.flights.application;

import java.io.IOException;
import java.util.*;

import com.mmt.flights.flightsutil.AirportDetailsUtil;
import com.mmt.flights.helper.CommonCurrencyConverter;
import com.mmt.flights.helper.CurrencyConverter;
import com.mmt.flights.helper.OandaCurrencyConverter;
import com.mmt.flights.pii.clients.ScramblerClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;
import com.mmt.flights.constants.Constants;

/**
 *         Application startup file which handles base package scanner and
 *         loading profile specific yaml file.Profile is controlled by provided
 *         profile as system variable on starting the application with values
 *         dev or prod.
 */
@SpringBootApplication
@EnableHystrix
@EnableHystrixDashboard
@ComponentScan(basePackages = { Constants.SPRING_PACKAGE_SCANNER })
public class PnrApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(PnrApplication.class);
        application.setEnvironment(ProfileUtil.loadProfile());
        application.run(args);
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
