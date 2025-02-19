package com.mmt.flights.application;

import com.mmt.flights.constants.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

/**
 *         loads the yaml file based on profile value. ex: If profile is DEV DEV
 *         yaml files is loaded.
 */
public class ProfileUtil {

	public static ConfigurableEnvironment loadProfile() {
		ConfigurableEnvironment environment = new StandardEnvironment();
		if (StringUtils.isEmpty(System.getProperty(Constants.PROFILE)))
			throw new IllegalArgumentException("Profile not Specified.");
		environment.setActiveProfiles(System.getProperty(Constants.PROFILE));
		return environment;
	}
}
