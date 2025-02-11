package com.mmt.flights.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "flights.pnr.hystrix.cms")
public class CMSHystrixConfig {

	private int circuitBreakerErrorThresholdPercentage;
	private int circuitBreakerRequestVolumeThreshold;
	private int circuitBreakerSleepWindowInMilliseconds;
	private int concurrentRequest;
	private int executionTimeoutInMilliseconds;
	private int rollingStatisticalWindowInMilliseconds;
	private int maxQueueSize;
	private int maxPoolSize;

	public int getCircuitBreakerErrorThresholdPercentage() {
		return circuitBreakerErrorThresholdPercentage;
	}

	public void setCircuitBreakerErrorThresholdPercentage(int circuitBreakerErrorThresholdPercentage) {
		this.circuitBreakerErrorThresholdPercentage = circuitBreakerErrorThresholdPercentage;
	}

	public int getCircuitBreakerRequestVolumeThreshold() {
		return circuitBreakerRequestVolumeThreshold;
	}

	public void setCircuitBreakerRequestVolumeThreshold(int circuitBreakerRequestVolumeThreshold) {
		this.circuitBreakerRequestVolumeThreshold = circuitBreakerRequestVolumeThreshold;
	}

	public int getCircuitBreakerSleepWindowInMilliseconds() {
		return circuitBreakerSleepWindowInMilliseconds;
	}

	public void setCircuitBreakerSleepWindowInMilliseconds(int circuitBreakerSleepWindowInMilliseconds) {
		this.circuitBreakerSleepWindowInMilliseconds = circuitBreakerSleepWindowInMilliseconds;
	}

	public int getConcurrentRequest() {
		return concurrentRequest;
	}

	public void setConcurrentRequest(int concurrentRequest) {
		this.concurrentRequest = concurrentRequest;
	}

	public int getExecutionTimeoutInMilliseconds() {
		return executionTimeoutInMilliseconds;
	}

	public void setExecutionTimeoutInMilliseconds(int executionTimeoutInMilliseconds) {
		this.executionTimeoutInMilliseconds = executionTimeoutInMilliseconds;
	}

	public int getRollingStatisticalWindowInMilliseconds() {
		return rollingStatisticalWindowInMilliseconds;
	}

	public void setRollingStatisticalWindowInMilliseconds(int rollingStatisticalWindowInMilliseconds) {
		this.rollingStatisticalWindowInMilliseconds = rollingStatisticalWindowInMilliseconds;
	}

	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

}
