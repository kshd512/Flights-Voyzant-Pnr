package com.mmt.flights.common.constants;

public class EndPoints {
    public static final String PROFILE = "profile";

    public static final String SPRING_PACKAGE_SCANNER = "com.mmt";
    public static final String HEALTH_END_POINT = "/healthcheck";

    public static final String V1 = "/v1";
    public static final String V2 = "/v2";
    public static final String V3 = "/v3";
    public static final String PNR = "/pnr";
    public static final String CANCEL = "/cancel";
    public static final String SEARCH = "/date-change-search";
    public static final String PRE_PAYMENT = "/date-change-prepayment";
    public static final String COMMIT = "/date-change-commit";
    public static final String VALIDATE = "/validate";
    public static final String SPLIT_PNR = "/split";

    public static final String PNR_V1 = V1  + PNR;
    public static final String CANCEL_VALIDATE_V1 = V1 + CANCEL + VALIDATE;
    public static final String CANCEL_V1 = V1  + CANCEL;

    public static final String ODC_SEARCH_V1 = V1 + SEARCH ;
    public static final String ODC_PRE_PAYMENT = V1 + PRE_PAYMENT ;
    public static final String ODC_COMMIT = V1 + COMMIT ;


    public static final String ODC_SEARCH_V2 = V2 + SEARCH ;
    public static final String ODC_PRE_PAYMENT_V2 = V2 + PRE_PAYMENT ;
    public static final String ODC_COMMIT_V2 = V2 + COMMIT ;
    public static final String ODC_SEARCH_V3 = V3 + SEARCH ;

    public static final String BOARDING_PASS = "/boardingPass";
    public static final String BOARDING_PASS_V1 = V1 + BOARDING_PASS;
    public static final String BOARDING_PASS_VALIDATE = V1 + BOARDING_PASS+ VALIDATE;

    public static final String GET_FLIGHTS_INFO = V1 + "/getFlightsInfo";
    public static final String SPLIT = V1 + SPLIT_PNR;
}
