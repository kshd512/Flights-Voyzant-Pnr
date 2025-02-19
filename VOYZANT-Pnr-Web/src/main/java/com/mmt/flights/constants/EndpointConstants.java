package com.mmt.flights.constants;

public class EndpointConstants {

	public static final String V1 = "/v1";
	public static final String V2 = "/v2";
	public static final String REPRICE_PNR = "/repricepnr";
	public static final String REPRICE_PNR_V1 = V1 + REPRICE_PNR;
	public static final String HEALTH_END_POINT = "/health";
	public static final String ERROR_END_POINT = "/error";
	public static final String RETRIEVE_PNR = V1 + "/pnr";

	public static final String CANCEL_PNR = "/cancel";
	public static final String CANCEL_PNR_V1 = V1 + CANCEL_PNR;
	public static final String VOID_CANCEL = CANCEL_PNR_V1 + "/void";
	public static final String VALIDATE_CANCEL_V1 = CANCEL_PNR_V1 + "/validate";
	public static final String PNR_CANCEL_RELEASE = CANCEL_PNR_V1 + "/release";
	public static final String CHECK_REFUND = V1 + "/refund/check";
}
