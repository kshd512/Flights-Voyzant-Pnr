package com.mmt.flights.cms;

import com.mmt.flights.common.constants.CommonConstants;
import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.common.properties.CMSProps;
import com.mmt.flights.common.util.CommonUtil;
import com.mmt.flights.entity.cms.CMSDetailRequest;
import com.mmt.flights.entity.cms.CMSDetailResponse;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CMSService {

	@Autowired
	private CMSProps cmsProps;
	
	@Autowired
	private CommonUtil commonUtil;

	public CMSDetailResponse getCredentialDetail(CMSDetailRequest request) {
		try {
			long start = System.currentTimeMillis();

			CMSDetailResponse response = getFromService(cmsProps.getCredDetailUrl(), request, CMSDetailResponse.class,"CMS getCredentialDetail");

			long diff = System.currentTimeMillis() - start;

			MMTLogger.info((new LogParams.LogParamsBuilder())
							.timeTaken(diff)
							.serviceName("CMS_DETAILS_REQUEST_LATENCY")
							.className(this.getClass().getName())
							.request(request.getCredID())
							.build(),
					MetricType.LOG_FILE,MetricType.LOG_TIME);

			if (response == null || response.getPropMap() == null ||response.getPropMap().size() == 0) {
				throw new PSErrorException(PSCommonErrorEnum.EMPTY_CMS_INFO);
			}
			MMTLogger.info((new LogParams.LogParamsBuilder())
							.className(this.getClass().getName())
							.request(request.getCredID())
							.extraInfo("CMS cred Map size for credID=" + request.getCredID() + "::" + response.getPropMap().size()).build(),
					MetricType.LOG_FILE);
			return response;
		} catch (IOException e) {
			throw new PSErrorException(PSCommonErrorEnum.EMPTY_CMS_INFO);
		}
	}

	private <Q, R> R getFromService(String url, Q request, Class<R> respClass, String logStringPrefix)
			throws IOException {
		return commonUtil.getFromService(url, request, respClass, (long) cmsProps.getReadTimeout(),
				(long) cmsProps.getConnectTimeout(), CommonConstants.HTTP_TEMPLATE_NAME_CMS);
	}
}
