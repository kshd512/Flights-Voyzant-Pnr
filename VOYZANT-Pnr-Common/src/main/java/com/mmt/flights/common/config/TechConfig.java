package com.mmt.flights.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 */
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "flights.pnr.tech")
public class TechConfig {

	private long pnrRetrieveTimeout;
	private long pnrCancelTimeout;
	
	public long getpnrRetrieveTimeout() {
		return pnrRetrieveTimeout;
	}

	public void setpnrRetrieveTimeout(long pnrRetrieveTimeout) {
		this.pnrRetrieveTimeout = pnrRetrieveTimeout;
	}

	public long getPnrCancelTimeout() {
		return pnrCancelTimeout;
	}

	public void setPnrCancelTimeout(long pnrCancelTimeout) {
		this.pnrCancelTimeout = pnrCancelTimeout;
	}
}