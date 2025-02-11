package com.mmt.flights.common.exceptions;

public class CacheGeneralException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public CacheGeneralException(){

	}

	public CacheGeneralException(Exception e){
		super(e);
	}

	public CacheGeneralException(String message){
		super(message);
	}

	public CacheGeneralException(Throwable cause) {
		super(cause);
	}

	public CacheGeneralException(String message, Throwable cause){
		super(message, cause);
	}

	public CacheGeneralException(String message, Throwable cause,boolean enableSuppression, boolean writableStackTrace){
		super(message, cause, enableSuppression, writableStackTrace);
	}

}