package com.mmt.flights.common.enums;


import com.mmt.flights.postsales.error.PSErrorEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;

public enum ErrorEnum implements PSErrorEnum {


	CURRENCY_CONVERSION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"18002"),
	PNR_PARTIALLY_REFUNDED(HttpStatus.BAD_REQUEST,"40062"),
	NO_TOKEN_FOUND(HttpStatus.BAD_REQUEST,"40063")
	;


	private final HttpStatus httpStatus;
	private final String errorCode;


	ErrorEnum(HttpStatus code, String errorCode) {
		this.httpStatus = code;
		this.errorCode = errorCode;

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
		return name();
	}

}
