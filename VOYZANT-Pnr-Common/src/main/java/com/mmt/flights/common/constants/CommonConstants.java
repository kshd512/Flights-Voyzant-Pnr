package com.mmt.flights.common.constants;



public class CommonConstants {


	public static final String SERVICE_NAME = "Flights-VOYZANT-Pnr";
	public static final String HTTP_TEMPLATE_NAME_CMS = "HTTP_TEMPLATE_NAME_CMS";
	public static final String SUPPLIER_NAME = "VOYZANT";
	public static final String CANCEL_BIN = "CNXL_BIN";
	public static final String VOID_BIN = "VOID_BIN";
	public static final int SESSION_EXPIRY_TIME = 7200;
	public static final String CANCEL_CACHE_KEY = "VOYZANT_CANCELLATION_";
	public static final String VOID_CACHE_KEY = "VOYZANT_VOID_";
	public static final String NON_NUMBERS = "[^0-9]";

	public static final String CREATED_ON = "CREATED_ON";
	public static final String FAILURE = "FAILURE";
	public static final String SUCCESS = "SUCCESS";
	public static final String PENDING = "PENDING";

	public static final String REFUND_SUPPLIER_TIMEOUT_ERROR = "This submission may time out, please check if created refund order, or else resubmit";
    public static final String REFUND_SUPPLIER_FAILED="Refund fee exceeds total ticket price Intercept refund request";
	public static final String SIMPLE_QUERY_BIN_NAME = "SMPLQRY";

	public static final String VERSION_V1 = "v1";
	public static final String VERSION_V2 = "v2";
}
