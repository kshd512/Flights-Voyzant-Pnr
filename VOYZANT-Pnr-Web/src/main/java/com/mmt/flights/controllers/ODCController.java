package com.mmt.flights.controllers;

import com.mmt.flights.common.config.TechConfig;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.common.util.AdapterUtil;
import com.mmt.flights.odc.commit.DateChangeCommitRequest;
import com.mmt.flights.odc.commit.DateChangeCommitResponse;
import com.mmt.flights.odc.common.AbstractDateChangeRequest;
import com.mmt.flights.odc.constant.RequestType;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentRequest;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentResponse;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.search.SimpleSearchResponse;
import com.mmt.flights.odc.service.ODCSearchFlowSubscriber;
import com.mmt.flights.odc.service.PnrServices;
import com.mmt.flights.odc.service.ODCCommitFlowSubscriber;
import com.mmt.flights.odc.service.ODCPrePaymentFlowSubscriber;
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

import static com.mmt.flights.common.constants.EndPoints.*;

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
            observableResponse.subscribe(new ODCSearchFlowSubscriber(deferredResult, request, startTime, techConfig.getPnrCancelTimeout(), operation));
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

    @ApiResponses(@ApiResponse(code = 200, message = "pre payment"))
    @RequestMapping(value = ODC_PRE_PAYMENT, method = RequestMethod.POST)
    public DeferredResult<ResponseEntity<DateChangePrePaymentResponse>> prePayment(@RequestBody DateChangePrePaymentRequest request) {
        DateChangePrePaymentResponse response = null;
        DeferredResult<ResponseEntity<DateChangePrePaymentResponse>> deferredResult = new DeferredResult<>(techConfig.getPnrCancelTimeout());
        long startTime = System.currentTimeMillis();
        RequestType operation = RequestType.ODC_PREPAYMENT;
        try {
            String logKey = request.getPnr();
            MMTLogger.info(
                    (new LogParams.LogParamsBuilder())
                            .correlationId(logKey)
                            .lob(request.getLob())
                            .src(request.getSrc())
                            .className(this.getClass().getName())
                            .extraInfo("ODC pre payment")
                            .serviceName(getRequestCounterMetric(operation))
                            .request(MMTLogger.convertToJson(request))
                            .build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
            Observable<DateChangePrePaymentResponse> observableResponse = pnrServices.odcPrePayment(request);
            observableResponse.subscribe(new ODCPrePaymentFlowSubscriber(deferredResult, request, startTime, techConfig.getPnrCancelTimeout(), operation));
        } catch (Exception e) {
            PSErrorEnum errorCode = ErrorEnum.INVALID_REQUEST;
            if (e instanceof PSErrorException) {
                errorCode = ((PSErrorException) e).getPsErrorEnum();
            }
            response = new DateChangePrePaymentResponse();
            response.setError(AdapterUtil.getErrorDetails(errorCode, e.getMessage()));
            logError(request, errorCode, MMTLogger.convertToJson(request), e,operation);
            deferredResult.setErrorResult(new ResponseEntity<>(
                    response,
                    errorCode.getHttpStatus()));
        }
        return deferredResult;
    }

    @ApiResponses(@ApiResponse(code = 200, message = "List flights for date change"))
    @RequestMapping(value = ODC_COMMIT, method = RequestMethod.POST)
    public DeferredResult<ResponseEntity<DateChangeCommitResponse>> commit(@RequestBody DateChangeCommitRequest request) {
        DateChangeCommitResponse response = null;
        DeferredResult<ResponseEntity<DateChangeCommitResponse>> deferredResult = new DeferredResult<>(techConfig.getPnrCancelTimeout());
        long startTime = System.currentTimeMillis();
        RequestType operation = RequestType.ODC_COMMIT;
        try {
            String logKey = request.getPnr();
            MMTLogger.info(
                    (new LogParams.LogParamsBuilder())
                            .correlationId(logKey)
                            .lob(request.getLob())
                            .src(request.getSrc())
                            .className(this.getClass().getName())
                            .extraInfo("ODC commit request")
                            .serviceName(getRequestCounterMetric(operation))
                            .request(MMTLogger.convertToJson(request))
                            .build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
            Observable<DateChangeCommitResponse> observableResponse = pnrServices.odcCommit(request);
            observableResponse.subscribe(new ODCCommitFlowSubscriber(deferredResult, request, startTime, techConfig.getPnrCancelTimeout(), operation));
        } catch (Exception e) {
            PSErrorEnum errorCode = ErrorEnum.INVALID_REQUEST;
            if (e instanceof PSErrorException) {
                errorCode = ((PSErrorException) e).getPsErrorEnum();
            }
            response = new DateChangeCommitResponse();
            response.setError(AdapterUtil.getErrorDetails(errorCode, e.getMessage()));
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
