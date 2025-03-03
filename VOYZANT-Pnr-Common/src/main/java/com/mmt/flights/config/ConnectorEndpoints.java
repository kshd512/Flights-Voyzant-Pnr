package com.mmt.flights.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "flights.pnr.connector.endpoints")
public class ConnectorEndpoints {
    private String fetchPnrURL;
    private String pnrCancelURL;
    private String checkRefundURL;
    private String voidCancelOrderURL;
    private String cancelReleaseUrl;
    private String pnrSplitURL;
    private String odcSearchURL;

    public String getPnrSplitURL() {
        return pnrSplitURL;
    }

    public void setPnrSplitURL(String pnrSplitURL) {
        this.pnrSplitURL = pnrSplitURL;
    }

    public String getOdcSearchURL() {
        return odcSearchURL;
    }

    public void setOdcSearchURL(String odcSearchURL) {
        this.odcSearchURL = odcSearchURL;
    }
}
