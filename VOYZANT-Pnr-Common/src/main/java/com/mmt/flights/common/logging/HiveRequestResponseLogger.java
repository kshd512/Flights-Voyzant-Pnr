package com.mmt.flights.common.logging;


import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.common.util.ScrambleUtil;
import com.mmt.flights.hivelogger.entity.HiveProperties;
import com.mmt.flights.hivelogger.logging.HiveManager;

import com.mmt.flights.odc.common.AbstractDateChangeRequest;
import com.mmt.flights.postsales.logger.FunnelStep;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.pnr.checkin.v1.request.SupplyWebCheckinRequestDTO;
import com.mmt.flights.supply.pnr.v4.request.SupplyPnrRequestDTO;
import com.mmt.flights.supply.pnr.v4.request.SupplySplitPnrRequestDTO;
import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Component
public class HiveRequestResponseLogger {

    private Logger logger = LoggerFactory.getLogger(HiveRequestResponseLogger.class);

    private HiveManager hiveManager;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final static String CANCEL_TOPIC_ID = "flights_cancellation";
    private final static String CANCEL_TEMPLATE_ID = "21294";

    private final static String ODC_TOPIC_ID = "flights_odc";
    private final static String ODC_TEMPLATE_ID = "21292";

    private final String TOPIC_ID_WEBCHECKIN = "flights_web_checkin";
    private final String TEMPLATE_ID_WEBCHECKIN = "21293";

    private final static String SPLIT_TOPIC_ID = "flights_split_pnr";
    private final static String SPLIT_TEMPLATE_ID = "22311";

    private final static String RETRIEVE_PNR_TOPIC_ID = "flights_pnr_details";
    private final static String RETRIEVE_TEMPLATE_ID = "26967";


    @Autowired
    @Qualifier("hivePropertiesColumbus")
    private HiveProperties hiveProperties;

    @PostConstruct
    public void init() throws IOException {
//        hiveManager = HiveManager.getInstance(hiveProperties);
    }

    public void logHiveData(String topic, String templateId, Map<String, Object> data) {
        hiveManager.logHiveUnionData(topic, templateId, data).subscribe(next -> {
            logger.debug("Pushed hive message: " + next);
        }, error -> {
            logger.error("Exception in pushing data to hive", error);
        });
    }


    public void logSplitPnrRequestResponse(HiveLogRequest logRequest, SupplySplitPnrRequestDTO request) {
        logHiveData(SPLIT_TOPIC_ID, SPLIT_TEMPLATE_ID, getSplitLogMap(logRequest, request));
    }

    public void logCancelRequestResponse(HiveLogRequest logRequest, SupplyPnrCancelRequestDTO cancelRequestDTO) {
        logHiveData(CANCEL_TOPIC_ID, CANCEL_TEMPLATE_ID, getCancelLogMap(logRequest, cancelRequestDTO));
    }

    public void logDateChangeRequestResponse(HiveLogRequest logRequest, AbstractDateChangeRequest odcRequest) {
        logHiveData(ODC_TOPIC_ID, ODC_TEMPLATE_ID, getODCLogMap(logRequest, odcRequest));
    }

    public void logBoardingRequestResponse(HiveLogRequest logRequest, SupplyWebCheckinRequestDTO webCheckinRequestDTO) {
        logHiveData(TOPIC_ID_WEBCHECKIN, TEMPLATE_ID_WEBCHECKIN, getBoardingLogMap(logRequest, webCheckinRequestDTO));
    }

    public void logHiveData(HiveLogRequest logRequest, SupplyPnrRequestDTO pnrRequestDTO) {
        logHiveData(RETRIEVE_PNR_TOPIC_ID, RETRIEVE_TEMPLATE_ID, getLogMap(logRequest, pnrRequestDTO));
    }

    public void logHiveData(SupplierStep dcStep, SupplyPnrRequestDTO supplyPnrRequest,
                            FunnelStep airlineOperation, String request, String response, long startTime, long endTime,
                            String errorCode, String errorMessage, String sessionId, String apiName, String statusCode, String status) {
        logHiveData(new HiveLogRequest(dcStep, airlineOperation, request, response, startTime,
                endTime, errorCode, sessionId, apiName, statusCode, errorMessage, status), supplyPnrRequest);
    }



    public void logCancelRequestResponse(SupplierStep supplierStep, SupplyPnrCancelRequestDTO cancelRequestDTO,
                                         FunnelStep funnelStep, String request, String response, long startTime, long endTime,
                                         String errorCode, String errorMessage, String sessionId, String apiName, String statusCode, String status) {

        logCancelRequestResponse(new HiveLogRequest(supplierStep, funnelStep, request, response, startTime,
                endTime, errorCode, sessionId, apiName, statusCode, errorMessage, status), cancelRequestDTO);
    }

    public void logSplitPnrRequestResponse(SupplierStep dcStep, SupplySplitPnrRequestDTO splitPnrRequest,
                                         FunnelStep airlineOperation, String request, String response, long startTime, long endTime,
                                         String errorCode, String errorMessage, String sessionId, String apiName, String statusCode, String status) {

        logSplitPnrRequestResponse(new HiveLogRequest(dcStep, airlineOperation, request, response, startTime,
                endTime, errorCode, sessionId, apiName, statusCode, errorMessage, status), splitPnrRequest);
    }

    public void logDateChangeRequestResponse(SupplierStep dcStep, AbstractDateChangeRequest dateChangeRequest,
                                             FunnelStep airlineOperation, String request, String response, long startTime, long endTime,
                                             String errorCode, String errorMessage, String sessionId, String apiName, String statusCode, String status) {

        logDateChangeRequestResponse(new HiveLogRequest(dcStep, airlineOperation, request, response, startTime,
                endTime, errorCode, sessionId, apiName, statusCode, errorMessage, status), dateChangeRequest);
    }

    public void logBoardingRequestResponse(SupplierStep dcStep, SupplyWebCheckinRequestDTO webCheckinRequestDTO,
                                           FunnelStep airlineOperation, String request, String response, long startTime, long endTime,
                                           String errorCode, String errorMessage, String sessionId, String apiName, String statusCode, String status) {
        logBoardingRequestResponse(new HiveLogRequest(dcStep, airlineOperation, request, response, startTime,
                endTime, errorCode, sessionId, apiName, statusCode, errorMessage, status), webCheckinRequestDTO);
    }






    private Map<String, Object> getODCLogMap(HiveLogRequest logRequest, AbstractDateChangeRequest odcRequest) {

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> context = new HashMap<>();
        context.put("server_timestamp", System.currentTimeMillis());
        context.put("templateID",ODC_TEMPLATE_ID);
        context.put("topicID",ODC_TOPIC_ID);

        data.put("topicID", ODC_TOPIC_ID);
        data.put("templateID", ODC_TEMPLATE_ID);
        data.put("SESSION_ID", logRequest.getSessionId());
        data.put("AIRLINE_CODE", odcRequest.getAirline());
        data.put("REQUEST_TIMESTAMP", simpleDateFormat.format(logRequest.getStartTime()));
        data.put("RESPONSE_TIMESTAMP", simpleDateFormat.format(logRequest.getEndTime()));
        data.put("LATENCY", String.valueOf(logRequest.getEndTime() - logRequest.getStartTime()));
        data.put("SERVER_IP", getServerIP());
        data.put("SUPPLIER_CODE", odcRequest.getSupplierCode());
        data.put("PNR_NUMBER", odcRequest.getPnr());

        data.put("API_REQUEST_TYPE", logRequest.getRequestType());
        data.put("ACTION", logRequest.getStep());

        data.put("API_NAME", logRequest.getApiName());
        data.put("API_REQUEST", logRequest.getRawRequest());
        data.put("API_RESPONSE", logRequest.getRawResponse());
        data.put("BOOKING_ID", odcRequest.getMmtId());

        data.put("RESPONSE_STATUS", logRequest.getStatus());
        data.put("HTTP_STATUS_CODE", logRequest.getStatusCode());
        data.put("CONN_ERROR_CODE", nullHandler(logRequest.getErrorCode()));
        data.put("CONN_ERROR_MSG", logRequest.getErrorMessage());
        data.put("SRC", odcRequest.getSrc());
        data.put("LOB", odcRequest.getLob());
        data.put("CONTEXT", context);

        return data;
    }


    private Map<String, Object> getCancelLogMap(HiveLogRequest logRequest, SupplyPnrCancelRequestDTO cancelRequestDTO) {

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> context = new HashMap<>();
        context.put("server_timestamp", System.currentTimeMillis());
        context.put("templateID",CANCEL_TEMPLATE_ID);
        context.put("topicID",CANCEL_TOPIC_ID);

        data.put("topicID", CANCEL_TOPIC_ID);
        data.put("templateID", CANCEL_TEMPLATE_ID);
        data.put("SESSION_ID", logRequest.getSessionId());
        data.put("AIRLINE_CODE", cancelRequestDTO.getRequestCore().getValidatingCarrier());
        data.put("REQUEST_TIMESTAMP", simpleDateFormat.format(logRequest.getStartTime()));
        data.put("RESPONSE_TIMESTAMP", simpleDateFormat.format(logRequest.getEndTime()));
        data.put("LATENCY", String.valueOf(logRequest.getEndTime() - logRequest.getStartTime()));
        data.put("SERVER_IP", getServerIP());
        data.put("SUPPLIER_CODE", cancelRequestDTO.getRequestCore().getSupplierName());
        data.put("PNR_NUMBER", cancelRequestDTO.getRequestCore().getSupplierPnr());

        data.put("API_REQUEST_TYPE", logRequest.getRequestType());
        data.put("ACTION", logRequest.getStep());

        data.put("API_NAME", logRequest.getApiName());
        data.put("API_REQUEST", logRequest.getRawRequest());
        data.put("API_RESPONSE", logRequest.getRawResponse());
        data.put("BOOKING_ID", cancelRequestDTO.getRequestConfig().getBookingId());
        data.put("REQUEST_ID", cancelRequestDTO.getRequestConfig().getCorrelationId());
        data.put("CANCELLATION_TYPE", getCancellationType(cancelRequestDTO));
       /* if(logRequest.getRequestType().equalsIgnoreCase(RequestType.PNR_VALIDATE_CANCEL.name())) {
            data.put("VALIDATION_STATUS_CODE", logRequest.getErrorCode());
        }*/
        data.put("STATUS", logRequest.getStatus());
        data.put("STATUS_CODE", logRequest.getStatusCode());
        data.put("ERROR_CODE", nullHandler(logRequest.getErrorCode()));
        data.put("ERROR_MESSAGE", logRequest.getErrorMessage());
        data.put("SRC", cancelRequestDTO.getRequestConfig().getSource());
        data.put("LOB", cancelRequestDTO.getRequestConfig().getLob());
        data.put("CONTEXT", context);

        return data;
    }

    private Map<String, Object> getBoardingLogMap(HiveLogRequest logRequest, SupplyWebCheckinRequestDTO webCheckinRequestDTO) {

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> context = new HashMap<>();
        context.put("server_timestamp", System.currentTimeMillis());
        context.put("templateID",TEMPLATE_ID_WEBCHECKIN);
        context.put("topicID",TOPIC_ID_WEBCHECKIN);

        data.put("SESSION_ID", logRequest.getSessionId());
        data.put("AIRLINE_CODE", webCheckinRequestDTO.getRequestCore().getValidatingCarrier());
        data.put("REQUEST_TIMESTAMP", simpleDateFormat.format(logRequest.getStartTime()));
        data.put("RESPONSE_TIMESTAMP", simpleDateFormat.format(logRequest.getEndTime()));
        data.put("LATENCY", String.valueOf(logRequest.getEndTime() - logRequest.getStartTime()));
        data.put("SERVER_IP", getServerIP());
        data.put("SUPPLIER_CODE", webCheckinRequestDTO.getRequestCore().getSupplierName());
        data.put("PNR_NUMBER", webCheckinRequestDTO.getRequestCore().getSupplierPnr());
        data.put("API_REQ_TYPE", logRequest.getRequestType());
        data.put("API_NAME", logRequest.getApiName());
        data.put("ACTION", logRequest.getStep());
        data.put("API_RAW_REQ", logRequest.getRawRequest());
        data.put("API_RAW_RESP", logRequest.getRawResponse());
        data.put("BOOKING_ID", webCheckinRequestDTO.getRequestConfig().getCorrelationId());
        data.put("REQUEST_ID", webCheckinRequestDTO.getRequestConfig().getCorrelationId());
        data.put("BOARDING_PASS_STATUS", logRequest.getStatus());
        data.put("STATUS_CODE", logRequest.getStatusCode());
        data.put("ERROR_CODE", nullHandler(logRequest.getErrorCode()));
        data.put("ERROR_MESSAGE", logRequest.getErrorMessage());
        data.put("SRC", webCheckinRequestDTO.getRequestConfig().getSource());
        data.put("LOB", webCheckinRequestDTO.getRequestConfig().getLob());
        data.put("CONTEXT", context);

        return data;
    }

    private String getCancellationType(SupplyPnrCancelRequestDTO cancelRequestDTO){
        if(CollectionUtils.isEmpty(cancelRequestDTO.getRequestCore().getFlightsList()) && CollectionUtils.isEmpty(cancelRequestDTO.getRequestCore().getPaxInfoList())){
            return "FullCancellation";
        } else if(!CollectionUtils.isEmpty(cancelRequestDTO.getRequestCore().getFlightsList())){
            return "PartialSegmentCancellation";
        } else if(!CollectionUtils.isEmpty(cancelRequestDTO.getRequestCore().getPaxInfoList())){
            return "PartialPaxCancellation";
        }
        return "UNSUPPORTED";
    }

    private String getServerIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "UNKNOWN HOST";
        }
    }

    private Map<String, Object> getSplitLogMap(HiveLogRequest logRequest, SupplySplitPnrRequestDTO splitRequest) {

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> context = new HashMap<>();
        context.put("server_timestamp", System.currentTimeMillis());
        context.put("templateID",SPLIT_TEMPLATE_ID);
        context.put("topicID",SPLIT_TOPIC_ID);

        data.put("topicID", SPLIT_TOPIC_ID);
        data.put("templateID", SPLIT_TEMPLATE_ID);
        data.put("SESSION_ID", logRequest.getSessionId());

        data.put("REQUEST_TIMESTAMP", simpleDateFormat.format(logRequest.getStartTime()));
        data.put("RESPONSE_TIMESTAMP", simpleDateFormat.format(logRequest.getEndTime()));
        data.put("LATENCY", String.valueOf(logRequest.getEndTime() - logRequest.getStartTime()));
        data.put("SERVER_IP", getServerIP());
        data.put("SUPPLIER_CODE", splitRequest.getRequestCore().getSupplierName());
        data.put("PNR_NUMBER", splitRequest.getRequestCore().getSupplierPnr());

        data.put("ACTION", logRequest.getStep());
        data.put("API_REQUEST", logRequest.getRawRequest());
        data.put("API_RESPONSE", logRequest.getRawResponse());
        data.put("BOOKING_ID", splitRequest.getRequestConfig().getBookingId());
        data.put("REQUEST_ID", splitRequest.getRequestConfig().getCorrelationId());

        data.put("STATUS", logRequest.getStatus());
        data.put("STATUS_CODE", logRequest.getStatusCode());
        data.put("ERROR_CODE", nullHandler(logRequest.getErrorCode()));
        data.put("ERROR_MESSAGE", logRequest.getErrorMessage());
        data.put("SRC", splitRequest.getRequestConfig().getSource());
        data.put("LOB", splitRequest.getRequestConfig().getLob());
        data.put("CONTEXT", context);

        return data;
    }

    private Map<String, Object> getLogMap(HiveLogRequest logRequest, SupplyPnrRequestDTO request) {

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> context = new HashMap<>();
        context.put("server_timestamp", System.currentTimeMillis());
        context.put("templateID",RETRIEVE_TEMPLATE_ID);
        context.put("topicID",RETRIEVE_PNR_TOPIC_ID);


        data.put("topicID", RETRIEVE_PNR_TOPIC_ID);
        data.put("templateID", RETRIEVE_TEMPLATE_ID);

        data.put("REQUEST_TIMESTAMP", simpleDateFormat.format(logRequest.getStartTime()));
        data.put("RESPONSE_TIMESTAMP", simpleDateFormat.format(logRequest.getEndTime()));
        data.put("LATENCY", String.valueOf(logRequest.getEndTime() - logRequest.getStartTime()));
        data.put("SERVER_IP", getServerIP());
        data.put("SUPPLIER_CODE", request.getSupplierName());
        data.put("PNR_NUMBER", request.getSupplierPnr());

        data.put("ACTION", logRequest.getStep());
        data.put("API_REQUEST", logRequest.getRawRequest());
        data.put("API_RESPONSE", logRequest.getRawResponse());

        data.put("STATUS", logRequest.getStatus());
        data.put("STATUS_CODE", logRequest.getStatusCode());
        data.put("ERROR_CODE", nullHandler(logRequest.getErrorCode()));
        data.put("ERROR_MESSAGE", logRequest.getErrorMessage());

        data.put("CONTEXT", context);

        return data;
    }

    public void pushEncryptedLogs(FlowState state, TaskLog log){
        String request = log.getRequest();
        String response = log.getResponse();
        String logKey = state.getValue(FlowStateKey.LOG_KEY);
        try {
            log.setRequest(ScrambleUtil.getStandardEncodedString(request,logKey));
            log.setResponse(ScrambleUtil.getStandardEncodedString(response,logKey));
        }catch (Exception e){
            MMTLogger.error(logKey,"Error while encoding "+log.getSupplierStep()+" REQ/RESP",this.getClass().getName(),e);
        }
        pushLogs(state,log);
    }

    public void pushLogs(FlowState state, TaskLog log) {
        try {
            Object request = state.getValue(FlowStateKey.REQUEST);
            FunnelStep funnelStep = state.getValue(FlowStateKey.FUNNEL_STEP);
            if (request != null) {
                if (request instanceof AbstractDateChangeRequest) {
                    logDateChangeRequestResponse(log.getSupplierStep(),
                            state.getValue(FlowStateKey.REQUEST, AbstractDateChangeRequest.class),
                            funnelStep,
                            log.getRequest(),
                            log.getResponse(),
                            log.getStartTime(),
                            log.getEndTime(), log.getErrorCode(), log.getErrorMessage(), "", "",
                            log.getHttpStatus(),
                            log.getStatus());

                } else if (request instanceof SupplyPnrCancelRequestDTO) {
                    logCancelRequestResponse(log.getSupplierStep(),
                            state.getValue(FlowStateKey.REQUEST, SupplyPnrCancelRequestDTO.class),
                            funnelStep,
                            log.getRequest(),
                            log.getResponse(),
                            log.getStartTime(),
                            log.getEndTime(), log.getErrorCode(), log.getErrorMessage(), "", "",
                            log.getHttpStatus(),
                            log.getStatus());
                } else if (request instanceof SupplyPnrRequestDTO) {
                    logHiveData(log.getSupplierStep(),
                            state.getValue(FlowStateKey.REQUEST, SupplyPnrRequestDTO.class),
                            funnelStep==null?FunnelStep.Get:funnelStep,
                            log.getRequest(),
                            log.getResponse(),
                            log.getStartTime(),
                            log.getEndTime(), log.getErrorCode(), log.getErrorMessage(), "", "",
                            log.getHttpStatus(),
                            log.getStatus());
                }
                MMTLogger.info((new LogParams.LogParamsBuilder())
                        .correlationId(state.getValue(FlowStateKey.LOG_KEY))
                        .serviceName(log.getSupplierStep().name()+"_NETWORK_CALL")
                        .type(funnelStep)
                        //.request(log.getRequest())
                        //.response(log.getResponse())
                        .timeTaken(log.getEndTime()-log.getStartTime()).build(), MetricType.LOG_FILE,MetricType.LOG_COUNTER, MetricType.LOG_TIME);
            }

        }
        catch (Exception e){
            MMTLogger.error("HiveLoggerError","Error while pushing logs",this.getClass().getName(),e);
        }
    }

    private String nullHandler(String str) {
        return (StringUtils.isBlank(str)) ? "null" : str;
    }

}
