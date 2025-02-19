package com.mmt.flights.common.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.flights.common.config.ConnectorTimeoutConfig;
import com.mmt.flights.httpclient.constants.HTTPClientConstants;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;


@Component
public class HttpClientUtil {


    RestTemplate restTemplate;

    @Autowired
    ConnectorTimeoutConfig connectorTimeoutConfig;

    private ObjectMapper objectMapper;

    private static final String PROTOBUF_TYPE = "application/x-protobuf";

    @PostConstruct
    public void init() {
        objectMapper = new ObjectMapper();

        SimpleClientHttpRequestFactory clientHttpRequestFactory
                = new SimpleClientHttpRequestFactory();
        //Connect timeout
        clientHttpRequestFactory.setConnectTimeout(connectorTimeoutConfig.getConnectTimeout());

        //Read timeout
        clientHttpRequestFactory.setReadTimeout(connectorTimeoutConfig.getReadTimeout());

        restTemplate = new RestTemplate(clientHttpRequestFactory);
    }



    public ResponseEntity<byte[]> postProto(String url, byte[] request)  {
        HttpEntity<?> httpEntity = getRequestEntity( request);
        try {
            return restTemplate.exchange(url, HttpMethod.POST, httpEntity, byte[].class);
        }  catch (HttpClientErrorException | HttpServerErrorException e) {
            return new ResponseEntity<>(e.getResponseBodyAsByteArray(), e.getStatusCode());
        }
    }

    private HttpEntity<byte[]> getRequestEntity(byte[] body) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, PROTOBUF_TYPE);
        headers.add(HttpHeaders.CONTENT_TYPE, PROTOBUF_TYPE);
        return new HttpEntity<>(body, headers);
    }


    public <Q, R> R post(String url, Q request, Class<R> respClass, Map<String,String> cmsMap) throws IOException {
        return execute(url, HttpMethod.POST, respClass, request, cmsMap);
    }

    private <R, Q> R execute(String url, HttpMethod method, Class<R> respClass, Q request, Map<String,String> cmsMap) throws IOException {
        HttpEntity<?> httpEntity = getRequestEntity( request,cmsMap);
        try {
            return restTemplate.exchange(url, method, httpEntity, respClass).getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            if(respClass.equals(String.class)){
               return (R)e.getResponseBodyAsString();
            }
            return objectMapper.readValue(e.getResponseBodyAsByteArray(), respClass);
        } catch (ResourceAccessException e) {
            Throwable mostSpecificCause = e.getMostSpecificCause();
            if (mostSpecificCause instanceof SocketTimeoutException) {
                throw new PSErrorException(mostSpecificCause.getMessage() + " : could not read response in " + connectorTimeoutConfig.getReadTimeout() + " ms", PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT);
            }
            throw e;
        }
    }

    private <T> HttpEntity<T> getRequestEntity(T body, Map<String,String> cmsMap) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, HTTPClientConstants.JSON);
        if (body == null) {
            return new HttpEntity<T>(headers);
        }
        headers.add(HttpHeaders.CONTENT_TYPE, HTTPClientConstants.JSON);
        return new HttpEntity<T>(body, headers);
    }


}
