package com.mmt.flights.common.logging;

import com.mmt.flights.postsales.logger.FunnelStep;


public class HiveLogRequest {

    private String sessionId;
    private long startTime;
    private long endTime;
    private String requestType;
    private String step;
    private String rawRequest;
    private String rawResponse;
    private String errorCode;
    private String apiName;
    private String status;
    private String statusCode;
    private String errorMessage;

    public HiveLogRequest(SupplierStep step,
                          FunnelStep airlineOperation, String request, String response, long startTime, long endTime,
                          String errorCode, String sessionId, String apiName, String statusCode, String errorMessage, String status) {
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requestType = airlineOperation.name();
        this.step = step.name();
        this.rawRequest = request;
        this.rawResponse = response;
        this.errorCode = errorCode;
        this.apiName = apiName;
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.status = status;

    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getRawRequest() {
        return rawRequest;
    }

    public void setRawRequest(String rawRequest) {
        this.rawRequest = rawRequest;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
