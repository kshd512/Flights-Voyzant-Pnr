package com.mmt.flights.cms.hystrix;

import com.mmt.flights.cms.CMSService;
import com.mmt.flights.common.config.CMSHystrixConfig;
import com.mmt.flights.common.constants.HystrixConstants;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.entity.cms.CMSDetailRequest;
import com.mmt.flights.entity.cms.CMSDetailResponse;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Hystrix wrapper for CMS details call.
 */
@Component
public class CMSDetailsHystrixCommand implements ApplicationListener<EnvironmentChangeEvent> {

	@Autowired
	private CMSHystrixConfig hystrixConfig;

	@Autowired
	private CMSService cmsEngine;

	@PostConstruct
	private void setHystrixProperties() {

		ConfigurationManager.getConfigInstance().setProperty(
				"hystrix.command." + HystrixConstants.CMS_DETAILS_NAME
						+ ".execution.isolation.thread.timeoutInMilliseconds",
				hystrixConfig.getExecutionTimeoutInMilliseconds());
		ConfigurationManager.getConfigInstance().setProperty(
				"hystrix.command." + HystrixConstants.CMS_DETAILS_NAME + ".circuitBreaker.errorThresholdPercentage",
				hystrixConfig.getCircuitBreakerErrorThresholdPercentage());
		ConfigurationManager.getConfigInstance().setProperty(
				"hystrix.command." + HystrixConstants.CMS_DETAILS_NAME + ".circuitBreaker.requestVolumeThreshold",
				hystrixConfig.getCircuitBreakerRequestVolumeThreshold());
		ConfigurationManager.getConfigInstance().setProperty(
				"hystrix.command." + HystrixConstants.CMS_DETAILS_NAME + ".circuitBreaker.sleepWindowInMilliseconds",
				hystrixConfig.getCircuitBreakerSleepWindowInMilliseconds());
		ConfigurationManager.getConfigInstance().setProperty(
				"hystrix.command." + HystrixConstants.CMS_DETAILS_NAME + ".metrics.rollingStats.timeInMilliseconds",
				hystrixConfig.getRollingStatisticalWindowInMilliseconds());
		ConfigurationManager.getConfigInstance().setProperty(
				"hystrix.command." + HystrixConstants.CMS_DETAILS_NAME
						+ ".execution.isolation.semaphore.maxConcurrentRequests",
				hystrixConfig.getConcurrentRequest());
		ConfigurationManager.getConfigInstance().setProperty(
				"hystrix.threadpool." + HystrixConstants.CMS_DETAILS_GRP_NAME + ".maximumSize",
				hystrixConfig.getMaxPoolSize());
		ConfigurationManager.getConfigInstance().setProperty(
				"hystrix.threadpool." + HystrixConstants.CMS_DETAILS_GRP_NAME + ".allowMaximumSizeToDivergeFromCoreSize",
				true);
		ConfigurationManager.getConfigInstance().setProperty(
				"hystrix.threadpool." + HystrixConstants.CMS_DETAILS_GRP_NAME + ".maxQueueSize",
				hystrixConfig.getMaxQueueSize());
		ConfigurationManager.getConfigInstance().setProperty(
				"hystrix.threadpool." + HystrixConstants.CMS_DETAILS_GRP_NAME + ".queueSizeRejectionThreshold",
				hystrixConfig.getMaxQueueSize());
	}

	public void onApplicationEvent(EnvironmentChangeEvent event) {
		MMTLogger.info("", "Refreshing consul properties", CMSDetailsHystrixCommand.class.getName());
		setHystrixProperties();
	}

	@HystrixCommand(fallbackMethod = "defaultFallBack", commandKey = HystrixConstants.CMS_DETAILS_NAME, groupKey = HystrixConstants.CMS_DETAILS_GRP_NAME)
	public CMSDetailResponse run(CMSDetailRequest cmdRequest, String logKey) throws Exception {
		try {
			CMSDetailResponse response = cmsEngine.getCredentialDetail(cmdRequest);
			if (response == null) {
				throw new RuntimeException("CMSDetailResponse is null.");
			} else {
				Map<String, String> cmsDetailsMap = response.getPropMap();
				if (cmsDetailsMap == null || cmsDetailsMap.size() == 0) {
					throw new RuntimeException("CMSDetailResponse data map is null.");
				}
			}
			return response;
		} catch (Exception e) {
			MMTLogger.error(logKey, "Error in CMS Details response for " + ((cmdRequest.getCredID() != null) ? cmdRequest.getCredID() : ""),
					CMSDetailsHystrixCommand.class.getName(), e);
			throw e;
		}
	}

	public CMSDetailResponse defaultFallBack(CMSDetailRequest cmdRequest, String logKey, Throwable t) {
		return null;
	}
}