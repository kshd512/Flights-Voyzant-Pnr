package com.mmt.flights.common.logging;


import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorEnum;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import org.apache.commons.lang3.StringUtils;

public class TaskLog {
    private final SupplierStep supplierStep;
    private  String request;
    private final long startTime;
    private String url;
    private String response;
    private PSErrorEnum error;
    private long endTime;
    private String header;

    public TaskLog(SupplierStep supplierStep) {
        this.supplierStep = supplierStep;
        error = PSCommonErrorEnum.OK;
        startTime = System.currentTimeMillis();
    }

    public TaskLog(SupplierStep supplierStep, String request, String url) {
        this.url = url;
        this.supplierStep = supplierStep;
        this.request = request;
        error = com.mmt.flights.postsales.error.PSCommonErrorEnum.OK;
        startTime = System.currentTimeMillis();
    }


    public void setRequest(String request) {
        this.request = request;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public PSErrorEnum getError() {
        return error;
    }

    public void setError(PSErrorEnum error) {
        this.error = error;
        endTime = System.currentTimeMillis();
    }

    public SupplierStep getSupplierStep() {
        return supplierStep;
    }

    public String getRequest() {
        return request;
    }

    public String getResponse() {
        if (StringUtils.isBlank(response)) {
            return "";
        }
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
        endTime = System.currentTimeMillis();
    }


    public String getErrorCode() {
        if (error == com.mmt.flights.postsales.error.PSCommonErrorEnum.OK) {
            return "";
        }
        return error.getCode();
    }

    public String getErrorMessage() {
        if (error == com.mmt.flights.postsales.error.PSCommonErrorEnum.OK) {
            return "";
        }
        return error.toString();
    }

    public String getHttpStatus() {
        return error.getHttpStatus().toString();
    }

    public String getStatus() {
        if (error == com.mmt.flights.postsales.error.PSCommonErrorEnum.OK) {
            return SupplyStatus.SUCCESS.toString();
        }
        return SupplyStatus.FAILURE.toString();
    }



    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
