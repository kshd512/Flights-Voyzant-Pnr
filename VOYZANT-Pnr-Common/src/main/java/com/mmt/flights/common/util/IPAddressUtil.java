package com.mmt.flights.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.mmt.flights.common.constants.CommonConstants;
import com.mmt.flights.common.logging.MMTLogger;

public class IPAddressUtil {
	
	public static final String X_FORWARDED_FOR = "X-FORWARDED-FOR";

	private IPAddressUtil() {

	}

	public static String getClientIp(HttpServletRequest request) {
		String remoteAddr = "";
		if (request != null) {
			remoteAddr = request.getHeader(X_FORWARDED_FOR);
			if (StringUtils.isEmpty(remoteAddr)) {
				remoteAddr = request.getRemoteAddr();
			}
		}
		return remoteAddr;
	}

	public static String getIPAddress() {
		String ip = "";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			MMTLogger.error(CommonConstants.SERVICE_NAME, "Error in getting ip address", IPAddressUtil.class.getName(), e);
		}
		return ip;
	}
}
