package com.mmt.flights.common.exceptions;

import org.springframework.http.HttpStatus;
import com.mmt.flights.common.enums.ErrorEnum;

public class ServiceErrorException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private ErrorEnum errorDescriptor;
    private HttpStatus httpStatus;
    public ServiceErrorException(String message, ErrorEnum ed) {
        this(message, ed, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    public ServiceErrorException(String message, ErrorEnum ed, HttpStatus status) {
        super(message);
        this.setErrorDescriptor(ed);
        this.setHttpStatus(status);
    }
    public ErrorEnum getErrorDescriptor() {
        return errorDescriptor;
    }
    public void setErrorDescriptor(ErrorEnum errorDescriptor) {
        this.errorDescriptor = errorDescriptor;
    }
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
