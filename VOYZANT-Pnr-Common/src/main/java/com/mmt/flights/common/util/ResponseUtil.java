package com.mmt.flights.common.util;


import com.mmt.flights.flightsutil.DateUtil;
import com.mmt.flights.flightutil.entity.AirportDetails;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.util.AirportDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Component
public class ResponseUtil {

    @Autowired
    private AirportDetailsService  airportDetailsService;

    private static final String DATE_FORMAT_ddMMyy_HH_MM = "ddMMyy HHmm";
    public static final String YYYY_MM_DD_SPACE_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String YYYY_MM_DD_SPACE_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public String getDate(String formattedDate) {
        try {
            return DateUtil.formatDate(formattedDate, DateUtil.YYYY_MM_DD_T_HH_MM_SS, YYYY_MM_DD_SPACE_HH_MM);
        } catch (ParseException e) {
            throw new PSErrorException(formattedDate + " couldn't be parsed", PSCommonErrorEnum.FLT_UNKNOWN_ERROR);
        }
    }

    public String getFormatedDateTime(String formattedDate, String fromDateFormat, String toDateFormat) {
        try {
            return DateUtil.formatDate(formattedDate, fromDateFormat, toDateFormat);
        } catch (ParseException e) {
            throw new PSErrorException(formattedDate + " couldn't be parsed", PSCommonErrorEnum.FLT_UNKNOWN_ERROR);
        }
    }

    public Long getDurationInMins(String depDate, String depTimeZone, String arrDate, String arrTimeZone) {
        Long duration = new Long(0);
        try {
            duration = DateUtil.getDurationInMinutes(DateUtil.parseDate(depDate,YYYY_MM_DD_SPACE_HH_MM), depTimeZone,
                    DateUtil.parseDate(arrDate, YYYY_MM_DD_SPACE_HH_MM), arrTimeZone, DateUtil.DDMMYY_HHMM);
        } catch (ParseException e) {
            throw new PSErrorException(e.getMessage(), PSCommonErrorEnum.FLT_UNKNOWN_ERROR);
        }
        return duration;
    }

    public String getAirportName(String airportCode) {
        AirportDetails ad = null;
        ad = airportDetailsService.getAirportDetailsUtil().getAirportDetailsFromCityCode(airportCode);
        if (ad != null) {
            return ad.getNm();
        }
        return "";
    }

    public String getTimeZone(String airportCode) {
        AirportDetails ad = null;
        ad = airportDetailsService.getAirportDetailsUtil().getAirportDetailsFromCityCode(airportCode);
        if (ad != null) {
            return ad.getTimeZone();
        }
        return "";
    }
}
