package com.mmt.flights.controllers;

import com.mmt.flights.common.config.TechConfig;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.common.util.AdapterUtil;
import com.mmt.flights.odc.common.AbstractDateChangeRequest;
import com.mmt.flights.odc.constant.RequestType;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.search.SimpleSearchResponse;
import com.mmt.flights.odc.service.ODCCommonFlowSubscriber;
import com.mmt.flights.odc.service.PnrServices;
import com.mmt.flights.odc.util.ODCUtil;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import com.mmt.flights.postsales.error.PSErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;

import static com.mmt.flights.common.constants.EndPoints.DATE_CHANGE_SEARCH_V1;

@RestController
public class ODCController {

    @Autowired
    private TechConfig techConfig;

    @Autowired
    private PnrServices pnrServices;

    @ApiResponses(@ApiResponse(code = 200, message = "List flights for date change"))
    @RequestMapping(value = DATE_CHANGE_SEARCH_V1, method = RequestMethod.POST)
    public DeferredResult<ResponseEntity<SimpleSearchResponseV2>> odcSearchV1(@RequestBody DateChangeSearchRequest request) {
        SimpleSearchResponse response = null;
        DeferredResult<ResponseEntity<SimpleSearchResponseV2>> deferredResult = new DeferredResult<>(techConfig.getPnrCancelTimeout());
        long startTime = System.currentTimeMillis();
        RequestType operation = RequestType.ODC_SEARCH;
        try {
            String logKey = request.getPnr();
            MMTLogger.info(
                    (new LogParams.LogParamsBuilder())
                            .correlationId(logKey)
                            .lob(request.getLob())
                            .src(request.getSrc())
                            .className(this.getClass().getName())
                            .extraInfo("ODC search request")
                            .serviceName(getRequestCounterMetric(operation))
                            .request(MMTLogger.convertToJson(request))
                            .build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);

            Observable<SimpleSearchResponseV2> observableResponse = pnrServices.odcSearch(request);
            observableResponse.subscribe(new ODCCommonFlowSubscriber(deferredResult, request, startTime, techConfig.getPnrCancelTimeout(), operation));
        } catch (Exception e) {
            PSErrorEnum errorCode = ErrorEnum.INVALID_REQUEST;
            if (e instanceof PSErrorException) {
                errorCode = ((PSErrorException) e).getPsErrorEnum();
            }
            response = new SimpleSearchResponse();
            response.setError(ODCUtil.getErrorDetails(errorCode, e.getMessage()));
            logError(request, errorCode, MMTLogger.convertToJson(request), e,operation);
            deferredResult.setErrorResult(new ResponseEntity<>(
                    response,
                    errorCode.getHttpStatus()));
        }
        return deferredResult;
    }

    private String getRequestCounterMetric(RequestType operation){
        return operation.name()+"_"+ MetricServices.REQUEST_COUNTER.name();
    }

    void logError(AbstractDateChangeRequest config, PSErrorEnum errorCode, String requestJson, Throwable e, RequestType operation) {
        LogParams.LogParamsBuilder logBuilder = new LogParams.LogParamsBuilder()
                .className(this.getClass().getName())
                .throwable(e)
                .extraInfo("Controller error")
                .request(requestJson)
                .serviceName(AdapterUtil.getMetricServiceName(operation.name()+"_"+ MetricServices.REQUEST_ERROR.name(),errorCode));
        if (config != null) {
            logBuilder.correlationId(config.getPnr())
                    .lob(config.getLob())
                    .src(config.getSrc());
        }
        MMTLogger.error(
                logBuilder.build(),
                MetricType.LOG_FILE, MetricType.LOG_COUNTER);
    }
}
