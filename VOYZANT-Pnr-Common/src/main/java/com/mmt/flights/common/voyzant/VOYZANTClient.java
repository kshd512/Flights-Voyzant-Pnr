package com.mmt.flights.common.voyzant;

import com.mmt.flights.common.constants.HystrixConstants;
import com.mmt.flights.common.util.JaxbHandlerService;
import com.netflix.config.ConfigurationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
public class VOYZANTClient {

    @Autowired
    private VOYZANTHystrixProps VOYZANTHystrixProps;

    @Autowired
    private JaxbHandlerService jaxB;

    @PostConstruct
    private void setHystrixProperties() {

        ConfigurationManager.getConfigInstance().setProperty(
                "hystrix.command." + HystrixConstants.VOYZANT_PNR_NAME
                        + ".execution.isolation.thread.timeoutInMilliseconds",
                VOYZANTHystrixProps.getExecutionTimeoutInMilliseconds());
        ConfigurationManager.getConfigInstance().setProperty(
                "hystrix.command." + HystrixConstants.VOYZANT_PNR_NAME + ".circuitBreaker.errorThresholdPercentage",
                VOYZANTHystrixProps.getCircuitBreakerErrorThresholdPercentage());
        ConfigurationManager.getConfigInstance().setProperty(
                "hystrix.command." + HystrixConstants.VOYZANT_PNR_NAME + ".circuitBreaker.requestVolumeThreshold",
                VOYZANTHystrixProps.getCircuitBreakerRequestVolumeThreshold());
        ConfigurationManager.getConfigInstance().setProperty(
                "hystrix.command." + HystrixConstants.VOYZANT_PNR_NAME + ".circuitBreaker.sleepWindowInMilliseconds",
                VOYZANTHystrixProps.getCircuitBreakerSleepWindowInMilliseconds());
        ConfigurationManager.getConfigInstance().setProperty(
                "hystrix.command." + HystrixConstants.VOYZANT_PNR_NAME + ".metrics.rollingStats.timeInMilliseconds",
                VOYZANTHystrixProps.getRollingStatisticalWindowInMilliseconds());
        ConfigurationManager.getConfigInstance().setProperty(
                "hystrix.threadpool." + HystrixConstants.VOYZANT_PNR_GRP_NAME + ".maximumSize",
                VOYZANTHystrixProps.getMaxPoolSize());
        ConfigurationManager.getConfigInstance().setProperty(
                "hystrix.threadpool." + HystrixConstants.VOYZANT_PNR_GRP_NAME + ".maxQueueSize",
                VOYZANTHystrixProps.getMaxQueueSize());
    }
}
