package com.mmt.flights.cancel.service.handler;


import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.common.util.AdapterUtil;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyValidateCancelResponseDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;
import rx.Subscriber;

import java.util.concurrent.TimeUnit;

public class ValidateCancelSubscriber extends Subscriber<SupplyValidateCancelResponseDTO> {

    private final DeferredResult<ResponseEntity<SupplyValidateCancelResponseDTO>> defResult;
    private final SupplyPnrCancelRequestDTO request;
    private volatile SupplyValidateCancelResponseDTO response;

    private long subscriptionStartTime;
    private final long startTime;
    private final long serviceReplytimeout;

    public ValidateCancelSubscriber(DeferredResult<ResponseEntity<SupplyValidateCancelResponseDTO>> deferredResult,
                                    SupplyPnrCancelRequestDTO request, long startTime, long serviceReplytimeout) {
        this.defResult = deferredResult;
        this.request = request;
        this.startTime = startTime;
        this.serviceReplytimeout = serviceReplytimeout;
    }

    @Override
    public void onStart() {
        this.subscriptionStartTime = System.currentTimeMillis();
        long timeElapsedSinceStarted = this.subscriptionStartTime - startTime;
        long timeout = serviceReplytimeout - timeElapsedSinceStarted;
        Observable.timer(timeout, TimeUnit.MILLISECONDS).subscribe(item1 -> {
            if (!defResult.isSetOrExpired()) {
                SupplyValidateCancelResponseDTO resp = AdapterUtil.getErroneousResponseValidateCancel(HttpStatus.GATEWAY_TIMEOUT,
                        PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.getCode(), PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.getMessage(),
                        PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.getMessage());
                MMTLogger.error(
                        (new LogParams.LogParamsBuilder())
                                .serviceName(MetricServices.VALIDATE_CANCEL_TIME_OUT.name())
                                .className(ValidateCancelSubscriber.class.getName())
                                .src(request.getRequestConfig().getSource()).lob(request.getRequestConfig().getLob())
                                .correlationId(request.getRequestConfig().getCorrelationId())
                                .httpStatus(HttpStatus.GATEWAY_TIMEOUT.value())
                                .errorCode(PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.getCode())
                                .extraInfo("Setting Result because of Timeout in "
                                        + (System.currentTimeMillis() - startTime))
                                .build(),
                        MetricType.LOG_FILE, MetricType.LOG_COUNTER);
                defResult.setErrorResult(new ResponseEntity<SupplyValidateCancelResponseDTO>(resp, HttpStatus.GATEWAY_TIMEOUT));
                unsubscribe();
            }
        });
    }

    @Override
    public void onCompleted() {
        long timeElapsed = System.currentTimeMillis() - subscriptionStartTime;
        defResult.setResult(new ResponseEntity<SupplyValidateCancelResponseDTO>(response, getHttpStatusCode(response)));

        MMTLogger.info((new LogParams.LogParamsBuilder()).serviceName(MetricServices.VALIDATE_CANCEL_PNR_RETRIEVE_LATENCY.name())
                .className(ValidateCancelSubscriber.class.getName()).extraInfo("Cancel PNR Request completed")
                .src(request.getRequestConfig().getSource()).lob(request.getRequestConfig().getLob())
                .timeTaken(timeElapsed).build(), MetricType.LOG_FILE, MetricType.LOG_TIME);
    }

    private HttpStatus getHttpStatusCode(SupplyValidateCancelResponseDTO resp) {
        if (response == null)
            return HttpStatus.INTERNAL_SERVER_ERROR;
        HttpStatus httpStatus = HttpStatus.OK;
        if (SupplyStatus.FAILURE.equals(response.getStatus()) && response.getErrList() != null
                && response.getErrList().size() > 0) {
            String statusCode = response.getErrList().get(0).getStatusCode();
            httpStatus = HttpStatus.resolve(Integer.parseInt(statusCode));
        }
        return httpStatus;
    }

    @Override
    public void onError(Throwable e) {
        defResult.setErrorResult(new ResponseEntity<SupplyValidateCancelResponseDTO>(AdapterUtil.getErroneousResponseValidateCancel(
                HttpStatus.INTERNAL_SERVER_ERROR, PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode(),
                PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getMessage(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR));
        MMTLogger.error(
                (new LogParams.LogParamsBuilder())
                        .serviceName(MetricServices.VALIDATE_CANCEL_REQUEST_ERROR.name())
                        .className(ValidateCancelSubscriber.class.getName())
                        .src(request.getRequestConfig().getSource()).lob(request.getRequestConfig().getLob())
                        .correlationId(request.getRequestConfig().getCorrelationId()).throwable(e)
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .errorCode(PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode()).build(),
                MetricType.LOG_FILE, MetricType.LOG_COUNTER);
    }

    @Override
    public void onNext(SupplyValidateCancelResponseDTO resp) {
        response = resp;
        if (SupplyStatus.SUCCESS.equals(resp.getStatus())) {
            MMTLogger.info((new LogParams.LogParamsBuilder())
                            .serviceName(MetricServices.VALIDATE_CANCEL_REQUEST_SUCCESS.name())
                            .src(request.getRequestConfig().getSource()).lob(request.getRequestConfig().getLob())
                            .className(ValidateCancelSubscriber.class.getName()).extraInfo("PNR response received").build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
        }
    }
}
