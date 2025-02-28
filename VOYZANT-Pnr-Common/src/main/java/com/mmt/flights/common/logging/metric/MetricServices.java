package com.mmt.flights.common.logging.metric;

public enum MetricServices {
    // PNR Retrieve related metrics
    PNR_RETRIEVE_REQUEST_TOTAL("pnr.retrieve.request.total"),
    PNR_RETRIEVE_REQUEST_SUCCESS("pnr.retrieve.request.success"),
    PNR_RETRIEVE_LATENCY("pnr.retrieve.latency"),
    PNR_RETRIEVE_REQUEST_ERROR("pnr.retrieve.request.error"),
    PNR_RETRIEVE_TIME_OUT("pnr.retrieve.timeout"),
    PNR_RETRIEVE_NETWORK_CALL_LATENCY("pnr.retrieve.network.call.latency"),

    // PNR Cancel related metrics
    PNR_CANCEL_REQUEST_COUNTER("pnr.cancel.request.counter"),
    VOID_PNR_CANCEL_REQUEST_COUNTER("void.pnr.cancel.request.counter"),
    PNR_CANCEL_REQUEST_SUCCESS("pnr.cancel.request.success"),
    PNR_CANCEL_LATENCY("pnr.cancel.latency"),
    PNR_CANCEL_REQUEST_ERROR("pnr.cancel.request.error"),
    PNR_CANCEL_TIME_OUT("pnr.cancel.timeout"),
    PNR_CANCEL_RELEASE_TIME_OUT("pnr.cancel.release.timeout"),
    PNR_CANCEL_NETWORK_CALL_LATENCY("pnr.cancel.network.call.latency"),
    PNR_CANCEL_RELEASE_NETWORK_CALL_LATENCY("pnr.cancel.release.network.call.latency"),

    // PNR Cancel Release related metrics
    PNR_CANCEL_RELEASE_REQUEST_TOTAL("pnr.cancel.release.request.total"),
    PNR_CANCEL_RELEASE_REQUEST_ERROR("pnr.cancel.release.request.error"),
    PNR_CANCEL_RELEASE_REQUEST_LATENCY("pnr.cancel.release.request.latency"),
    PNR_CANCEL_RELEASE_REQUEST_SUCCESS("pnr.cancel.release.request.success"),

    // Refund related metrics
    CHECK_REFUND_REQUEST_COUNTER("check.refund.request.counter"),
    CHECK_REFUND_NETWORK_CALL_LATENCY("check.refund.network.call.latency"),

    // Validation related metrics
    VALIDATE_CANCEL_REQUEST_ERROR("validate.cancel.request.error"),
    VALIDATE_CANCEL_TIME_OUT("validate.cancel.timeout"),
    VALIDATE_CANCEL_PNR_RETRIEVE_LATENCY("validate.cancel.pnr.retrieve.latency"),
    VALIDATE_CANCEL_REQUEST_SUCCESS("validate.cancel.request.success"),

    // ODC related metrics
    REQUEST_COUNTER("request.counter"),
    REQUEST_ERROR("request.error"), 
    REQUEST_SUCCESS("request.success"),
    REQUEST_TIMEOUT("request.timeout"),
    REQUEST_LATENCY("request.latency"),

    // Other metrics
    VOID_NETWORK_CALL_LATENCY("void.network.call.latency"),
    ODC_PENALTY_NOT_FOUND("odc.penalty.not.found"),
    PNR_SPLIT_NETWORK_CALL_LATENCY("pnr.split.network.call.latency"),
    PNR_SPLIT_ADAPTER_LATENCY("pnr.split.adapter.latency");

    private final String value;

    MetricServices(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}