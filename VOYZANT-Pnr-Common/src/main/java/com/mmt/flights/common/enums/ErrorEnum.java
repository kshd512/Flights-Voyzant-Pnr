package com.mmt.flights.common.enums;

import com.mmt.flights.postsales.error.PSErrorEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;

public enum ErrorEnum implements PSErrorEnum {

    CURRENCY_CONVERSION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "18002"),
    PNR_PARTIALLY_REFUNDED(HttpStatus.BAD_REQUEST, "40062"),
    NO_TOKEN_FOUND(HttpStatus.BAD_REQUEST, "40063"),
    
    // PNR validation errors
    EXT_PNR_DOES_NOT_EXIST(HttpStatus.BAD_REQUEST, "40064", "PNR does not exist"),
    EXT_PNR_CANCELLED(HttpStatus.BAD_REQUEST, "40065", "PNR is already cancelled"),
    EXT_PAX_DOES_NOT_EXIST(HttpStatus.BAD_REQUEST, "40066", "Passenger does not exist in PNR"),
    EXT_FLIGHT_DOES_NOT_EXIST(HttpStatus.BAD_REQUEST, "40067", "Flight does not exist in PNR"),
    EXT_SEGMENT_CANCELLED_BY_AIRLINE(HttpStatus.BAD_REQUEST, "40068", "Flight segment is cancelled by airline"),
    EXT_ALREADY_BOARDED(HttpStatus.BAD_REQUEST, "40069", "Passenger already boarded"),
    EXT_PNR_IN_NO_SHOW_WINDOW(HttpStatus.BAD_REQUEST, "40070", "PNR is in no show window"),
    EXT_CHECKED_IN_PNR_CANCELLATION_UNSUPPORTED(HttpStatus.BAD_REQUEST, "40071", "Checked-in PNR cancellation not supported"),
    EXT_BALANCE_DUE_ERROR(HttpStatus.BAD_REQUEST, "40072", "Balance due exists on PNR"),
    EXT_FLIGHT_AIRLINE_CANCELED(HttpStatus.BAD_REQUEST, "40073", "Flight has been cancelled by airline"),
    EXT_ONLY_INFANT_CANCELLATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "40074", "Only infant cancellation not allowed"),
    INVALID_PARTIAL_PAX_CANCEL_REQUEST(HttpStatus.BAD_REQUEST, "40075", "Invalid partial passenger cancellation request"),
    EXT_CANNOT_SPLIT_PNR(HttpStatus.BAD_REQUEST, "40076", "Cannot split PNR"),
    EXT_PNR_NOT_TICKETED(HttpStatus.BAD_REQUEST, "40077", "PNR is not ticketed"),
    EXT_MISSING_SEGMENTS_IN_JOURNEY(HttpStatus.BAD_REQUEST, "40078", "Missing segments in journey");

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

    ErrorEnum(HttpStatus code, String errorCode) {
        this.httpStatus = code;
        this.errorCode = errorCode;
        this.message = name();
    }

    ErrorEnum(HttpStatus code, String errorCode, String message) {
        this.httpStatus = code;
        this.errorCode = errorCode;
        this.message = message;
    }

    static public PSErrorEnum getValue(String errorCode) {
        if (StringUtils.isNotBlank(errorCode)) {
            ErrorEnum[] values = ErrorEnum.values();
            for (PSErrorEnum value : values) {
                if (value.getCode().equals(errorCode)) {
                    return value;
                }
            }
        }
        return com.mmt.flights.postsales.error.PSCommonErrorEnum.FLT_UNKNOWN_ERROR;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
