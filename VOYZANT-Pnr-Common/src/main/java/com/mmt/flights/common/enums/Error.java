package com.mmt.flights.common.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author MMT5680
 *
 *         Error information.
 */
public class Error {
	@JsonProperty("ec")
	private String errorCode;
	@JsonProperty("em")
	private String errorMessage;
	@JsonProperty("ed")
	private String errorDescription;

	/**
	 * @return the errorCode
	 */

	public Error() {
		/*
		 * This is default constructor for ErrorDetails
		 */
	}

	public Error(String code, String message, String desciption) {
		this.errorCode = code;
		this.errorMessage = message;
		this.errorDescription = desciption;
	}

	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode
	 *            the errorCode to set
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @return the errorDescription
	 */
	public String getErrorDescription() {
		return errorDescription;
	}

	/**
	 * @param errorDescription
	 *            the errorDescription to set
	 */
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return "ErrorDetails [errorCode=" + errorCode + ", errorMessage=" + errorMessage + ", errorDescription="
				+ errorDescription + "]";
	}

}
