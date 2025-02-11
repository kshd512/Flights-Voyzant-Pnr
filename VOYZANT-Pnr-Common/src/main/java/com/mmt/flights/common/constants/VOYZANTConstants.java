package com.mmt.flights.common.constants;

public class VOYZANTConstants {

    public static final String CANCEL_PNR_URL = "https://trade.trip.com/fltconfigsystem/ApiDoc/en-us/Booking/DistributorApi/Alliance/OpenOrderCancel";
    public static final String CANCEL_UNTICKETED_PNR_URL = "http://%s/flightorderoperate/OpenOrderCancel.asmx";

    public static final String ONLINE = "online";
    public static final String TICKETED = "S";
    public static final String REFUNDED = "R";
    public static final String PARTIAL_REFUND = "T";
    public static final String FULLY_TICKETED = "P";
    public static final String PARTIALLY_TICKETED = "O";
    public static final String CANCELLED = "C";
    public static final String CANCELLATION_PENDING = "D";
    public static final String DONE = "D";
    public static final String VALIDATING_CARRIER = "PK";

    //V2
    public static final String FULLY_TICKETED_V2 = "FLIGHT_TICKETED";
    public static final String PARTIALLY_TICKETED_V2 = "FLIGHT_TICKETED_PART";
    public static final String CANCELLATION_PENDING_V2 = "FLIGHT_CANCELLING";
    public static final String PARTIAL_REFUND_V2 = "FLIGHT_UNSUBCRIBE_PART";
    public static final String REFUNDED_V2 = "FLIGHT_UNSUBCRIBE_ALL";
    public static final String CANCELLED_V2 = "FLIGHT_CANCELLED";


    public static final String ADULT = "ADT";
    public static final String CHILD = "CHD";
    public static final String INFANT = "INF";
    public static final String HEADER_USERNAME = "UserName";
    public static final String HEADER_PASSWORD = "UserPassword";
    public static final String FLIGHT_TICKETING_V2 = "FLIGHT_TICKETING";
    public static final String SPECIAL_FARE_INFO = "SpecialFareInfo";
    public static final String BRAND_NAME = "BrandName";
    public static final String IN_PROCESS = "IN_PROCESS";
    public static final String INTERCEPTED = "INTERCEPTED";




}
