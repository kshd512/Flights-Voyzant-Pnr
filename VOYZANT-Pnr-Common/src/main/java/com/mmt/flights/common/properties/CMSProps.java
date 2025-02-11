package com.mmt.flights.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "flights.pnr.cms")
public class CMSProps {

	private int readTimeout;
	private int connectTimeout;
	private String credDetailUrl;
	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public String getCredDetailUrl() {
		return credDetailUrl;
	}

	public void setCredDetailUrl(String credDetailUrl) {
		this.credDetailUrl = credDetailUrl;
	}
}
