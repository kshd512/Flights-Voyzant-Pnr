package com.mmt.flights.common.exceptions;

public class RetryException extends Exception {

	private static final long serialVersionUID = 1048113462159147114L;

	public RetryException(String message) {
		super(message);
	}

	public RetryException(Exception e) {
		super(e);
	}
}
