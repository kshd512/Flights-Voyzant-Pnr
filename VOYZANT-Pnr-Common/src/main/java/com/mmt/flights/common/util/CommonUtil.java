package com.mmt.flights.common.util;

import com.mmt.flights.common.config.ConnectorTimeoutConfig;
import com.mmt.flights.common.constants.CommonConstants;
import com.mmt.flights.common.constants.OpenXAncillaryConstants;
import com.mmt.flights.common.properties.CMSProps;
import com.mmt.flights.httpclient.constants.HTTPClientConstants;
import com.mmt.flights.httpclient.entity.*;
import com.mmt.flights.httpclient.manager.HttpClientManager;
import com.mmt.flights.httpclient.manager.HttpManager;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CommonUtil {
	private static final String JSON_TYPE = "application/json";

	@Autowired
	private ConnectorTimeoutConfig ancillaryOrderDetailConfig;
	
	private HttpClientManager httpClientManager;

	@Autowired
	private CMSProps cmsProps;

	@PostConstruct
	public void init() {
		Map<String, Map<HTTPClientConfig, Long>> config = new HashMap<String, Map<HTTPClientConfig, Long>>();
		addConfig(config, CommonConstants.HTTP_TEMPLATE_NAME_CMS, cmsProps.getReadTimeout(), cmsProps.getConnectTimeout());
		addConfig(config, OpenXAncillaryConstants.OPEN_X_ANCILLARY, ancillaryOrderDetailConfig.getReadTimeout(),
				ancillaryOrderDetailConfig.getConnectTimeout());
		httpClientManager = HttpManager.getInstance(config);
	}

	private void addConfig(Map<String, Map<HTTPClientConfig, Long>> config, String httpTemplateName, long readTimeout, long connectTimeout) {
		Map<HTTPClientConfig, Long> clientConfig = new HashMap<HTTPClientConfig, Long>();
		clientConfig.put(HTTPClientConfig.READ_TIMEOUT_MILLIS, readTimeout);
		clientConfig.put(HTTPClientConfig.CONNECT_TIMEOUT_MILLIS, connectTimeout);
		config.put(httpTemplateName, clientConfig);
	}

	public <Q, R> R getFromService(String url, Q request, Class<R> respClass, Long readTimeOut,
			Long connectTimeout, String templateName) throws IOException {

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", HTTPClientConstants.JSON);
		HTTPRequest<Q, R> httpRequest = new HTTPRequest.Builder<Q, R>().url(url).type(HTTPRequestType.POST)
				.request(request).httpClientTemplate(templateName).response(respClass)
				.contentType(HTTPClientConstants.JSON)
				.timeout(new HTTPRequestTimeOut(connectTimeout, readTimeOut, HTTPClientConstants.DEFAULT_WRITE_TIMEOUT))
				.headers(headers).build();
		HTTPResponse<R> response = httpClientManager.execute(httpRequest);
		if (response.getResponseCode() == 200) {
			return response.getResponse();
		} else {
			if (respClass == String.class){
				return (R)response.getErrorResponse();
			}
			return com.mmt.flights.flightsutil.JsonConvertor.convertJsonToObject(response.getErrorResponse(),
					respClass);
		}
	}

	public Response postService(String url, String requestJson, String templateName)
			throws IOException {
		OkHttpClient client = getClient(templateName);

		MediaType mediaType = MediaType.parse(JSON_TYPE);
		RequestBody body = RequestBody.create(mediaType, requestJson);
		Request request = new Request.Builder().url(url).post(body).addHeader("Content-Type", JSON_TYPE)
				.addHeader("Accept", JSON_TYPE).addHeader("Connection", "close").build();

		return client.newCall(request).execute();
	}

	public Response getService(String url, String templateName)
			throws IOException {
		OkHttpClient client = getClient(templateName);

		MediaType mediaType = MediaType.parse(JSON_TYPE);
		Request request = new Request.Builder().url(url).get().addHeader("Content-Type", JSON_TYPE)
				.addHeader("Accept", JSON_TYPE).addHeader("Connection", "close").build();

		return client.newCall(request).execute();
	}

	private OkHttpClient getClient(String templateName) {
		OkHttpClient client = httpClientManager.getOkHttpClient(templateName);
		if (client == null) {
			client = httpClientManager.getOkHttpClient(HTTPClientType.DEFAULT.name());
		}
		return client;
	}
}
