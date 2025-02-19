package com.mmt.flights.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by amit on 1/12/16.
 */
public class DateUtil {

	public static final String STANDARD_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
	public static final String STANDARD_DATE_TIME_FORMAT_SEC = "yyyy-MM-dd HH:mm:ss";
	public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd";
	public static final String JOURNEY_DATE_TIME_FORMAT = "ddMMYYHHmm";


	private DateUtil() {
		throw new AssertionError("Cannot instantiate utility class");
	}

	public static String convertStandardToDDMMYY(String yyyy_MM_dd) {
		try {
			return formatDateDDMMYY(new SimpleDateFormat(STANDARD_DATE_FORMAT).parse(yyyy_MM_dd));
		} catch (ParseException e) {
			return yyyy_MM_dd;
		}
	}

	public static String getStandardDateTimeFormat(Date date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtil.STANDARD_DATE_TIME_FORMAT);

		return simpleDateFormat.format(date);
	}

	public static String formatDateDDMMYY(Date date) {
		return new SimpleDateFormat("ddMMyy").format(date);
	}

	public static SimpleDateFormat journeyDateFormatter() {
		return new SimpleDateFormat(JOURNEY_DATE_TIME_FORMAT);
	}

	public static String getStandardDateFormat(Date date) {
		return new SimpleDateFormat(STANDARD_DATE_FORMAT).format(date);
	}

	public static String formatDateDDMMYYHHMM(Date date) {
		return new SimpleDateFormat("ddMMyyHHmm").format(date);
	}


	public static SimpleDateFormat standardFormatter() {
		return new SimpleDateFormat(STANDARD_DATE_TIME_FORMAT);
	}


	public static long getDuration(String from, String to) {
		try {
			SimpleDateFormat sdf = standardFormatter();
			Date depDate = sdf.parse(from);
			Date arrDate = sdf.parse(to);
			return (arrDate.getTime() - depDate.getTime()) / 60000;
		} catch (Exception e) {
			return 0;
		}
	}



	public static long getDurationInMinutes(Date depDateTimeStr, String depTimeZone, Date arrDateTimeStr,
			String arrTimeZone) throws ParseException  {

		Date depDate;
		Date arrDate;
		if ((depTimeZone != null && !"".equals(depTimeZone)) && (arrTimeZone != null && !"".equals(arrTimeZone))) {
			SimpleDateFormat dateFormat_DD_MM_YY_HH_MM = new SimpleDateFormat("ddMMyy HHmm");

			String date1Str = dateFormat_DD_MM_YY_HH_MM.format(depDateTimeStr);
			TimeZone depTZ = TimeZone.getTimeZone(depTimeZone);
			depDate = DateUtil.convertDateTime(date1Str, depTZ);

			TimeZone arrTZ = TimeZone.getTimeZone(arrTimeZone);
			String date2Str = dateFormat_DD_MM_YY_HH_MM.format(arrDateTimeStr);
			arrDate = DateUtil.convertDateTime(date2Str, arrTZ);
		} else {
			depDate = depDateTimeStr;
			arrDate = arrDateTimeStr;
		}
		return Math.abs((arrDate.getTime() - depDate.getTime()) / (60 * 1000));
	}
	public static Date convertDateTime(String dateTimeddMMyy_HHmm, TimeZone timezone) throws ParseException  {
	       
        SimpleDateFormat dateFormat_DD_MM_YY_HH_MM = new SimpleDateFormat("ddMMyy HHmm");
        dateFormat_DD_MM_YY_HH_MM.setTimeZone(timezone);
        return dateFormat_DD_MM_YY_HH_MM.parse(dateTimeddMMyy_HHmm);
    }

	public static String getJourneyDateTimeFormat(String dateTime) {
		try {
			return journeyDateFormatter().format(standardFormatter().parse(dateTime));
		} catch (Exception e) {
			//todo log
			return dateTime;
		}
	}

	public static String getDate(String date) {
		return parseDate(date).format(DateTimeFormatter.ofPattern(STANDARD_DATE_TIME_FORMAT));
	}


	private static OffsetDateTime parseDateV2(String dateString) {
		//"/Date(1705883700000+0700)/";
		Pattern pattern = Pattern.compile("/Date\\((\\d+)([\\+\\-]\\d{4})\\)/");
		Matcher matcher = pattern.matcher(dateString);

		if (matcher.matches()) {
			long milliseconds = Long.parseLong(matcher.group(1));
			String offset = matcher.group(2);

			return OffsetDateTime.ofInstant(
					java.time.Instant.ofEpochMilli(milliseconds),
					java.time.ZoneOffset.of(offset)
			);
		} else {
			throw new IllegalArgumentException("Invalid date format");
		}
	}


	public static OffsetDateTime parseDate(String dateTime) {
		//"/Date(1705883700000+0700)/";
		// Extract the data within the brackets
		String data = dateTime.substring(dateTime.indexOf("(") + 1, dateTime.indexOf(")"));

		String dateString = data;
		String offset = null;
		if (data.contains("+") || data.contains("-")) {
			// Extract the timestamp and timezone offset
			dateString = data.substring(0, data.length() - 5);
			offset = data.substring(data.length() - 5);
		}
		long milliseconds = Long.parseLong(dateString);
		if (offset != null) {
			return OffsetDateTime.ofInstant(
					java.time.Instant.ofEpochMilli(milliseconds),
					java.time.ZoneOffset.of(offset)
			);
		} else {
			return OffsetDateTime.ofInstant(
					java.time.Instant.ofEpochMilli(milliseconds),
					java.time.ZoneOffset.UTC
			);
		}
	}


	public static Date getGMTDate(String gmtDate){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(STANDARD_DATE_TIME_FORMAT_SEC);
		LocalDateTime localDateTime = LocalDateTime.parse(gmtDate, formatter);
		ZoneId gmtZone = ZoneId.of("GMT");
		ZonedDateTime gmtTime = ZonedDateTime.of(localDateTime, gmtZone);
		return Date.from(gmtTime.toInstant());
	}

	public static long getTimeDifference(String deptDate, String arvlDate) throws ParseException {
		Date dt1 = null;
		Date dt2 = null;
		try {

			SimpleDateFormat currentFormat = new SimpleDateFormat(STANDARD_DATE_TIME_FORMAT);
			dt2 = currentFormat.parse(arvlDate);
			dt1 = currentFormat.parse(deptDate);
		} catch (ParseException e) {
			throw e;
		}
		int timeInSeconds = (int)((dt2.getTime()-dt1.getTime()) / 1000L);
		return timeInSeconds / 60;
	}

}