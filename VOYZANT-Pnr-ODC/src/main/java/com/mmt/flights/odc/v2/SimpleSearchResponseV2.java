package com.mmt.flights.odc.v2;

import com.mmt.flights.odc.common.ErrorDetails;
import com.mmt.flights.odc.search.Flight;

import java.util.ArrayList;
import java.util.List;

public class SimpleSearchResponseV2 {
    private boolean success;
    private String traceId;
    private String pnr;
    private String supplierCode;
    private List<Flight> flights = new ArrayList<>();
    private ErrorDetails error;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }
}