package com.mmt.flights.entity.callback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mmt.flights.odc.commit.DateChangeCommitRequest;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class ODCCallbackRequest {
    private DateChangeCommitRequest dccRequest;
    private String status;
    private Long maxTicketingTime;

    public DateChangeCommitRequest getDccRequest() {
        return dccRequest;
    }

    public void setDccRequest(DateChangeCommitRequest dccRequest) {
        this.dccRequest = dccRequest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getMaxTicketingTime() { return maxTicketingTime; }

    public void setMaxTicketingTime(Long maxTicketingTime) { this.maxTicketingTime = maxTicketingTime; }
}
