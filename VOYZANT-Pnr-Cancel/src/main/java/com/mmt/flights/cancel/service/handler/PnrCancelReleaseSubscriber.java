package com.mmt.flights.cancel.service.handler;

import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.common.util.AdapterUtil;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import io.reactivex.Observable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Subscriber;

import java.util.concurrent.TimeUnit;

public class PnrCancelReleaseSubscriber extends Subscriber<SupplyPnrCancelResponseDTO> {

    private final DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> defResult;
    private final SupplyPnrCancelRequestDTO cancelRequest;
    private volatile SupplyPnrCancelResponseDTO response;

    private long subscriptionStartTime;
    private final long searchStartTime;
    private final long serviceReplyTimeout;

    public PnrCancelReleaseSubscriber(
            DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> deferredResult,
            SupplyPnrCancelRequestDTO cancelRequest2, long searchStartTime,
            long serviceReplyTimeout) {
        this.defResult = deferredResult;
        this.cancelRequest = cancelRequest2;
        this.searchStartTime = searchStartTime;
        this.serviceReplyTimeout = serviceReplyTimeout;
    }

    @Override
    public void onStart() {
        this.subscriptionStartTime = System.currentTimeMillis();
        long timeElapsedSinceSearchStarted = this.subscriptionStartTime - searchStartTime;
        long timeout = serviceReplyTimeout - timeElapsedSinceSearchStarted;
        Observable.timer(timeout, TimeUnit.MILLISECONDS).subscribe(item1 -> {
            if (!defResult.isSetOrExpired()) {
                SupplyPnrCancelResponseDTO resp = AdapterUtil.getErroneousResponse_CancelPnr(HttpStatus.GATEWAY_TIMEOUT,
                        PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.getCode(), PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.getMessage(),
                        PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.name());
                MMTLogger.error(
                        (new LogParams.LogParamsBuilder())
                                .correlationId(cancelRequest.getRequestConfig().getCorrelationId())
                                .lob(cancelRequest.getRequestConfig().getLob())
                                .serviceName(MetricServices.PNR_CANCEL_RELEASE_TIME_OUT.name())
                                .className(PnrCancelReleaseSubscriber.class.getName())
                                .request(MMTLogger.convertToJson(cancelRequest))
                                .httpStatus(HttpStatus.REQUEST_TIMEOUT.value())
                                .errorCode(PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.getCode())
                                .extraInfo("Setting Result because of Timeout in "
                                        + (System.currentTimeMillis() - searchStartTime))
                                .build(),
                        MetricType.LOG_FILE, MetricType.LOG_COUNTER);
                defResult.setErrorResult(new ResponseEntity<SupplyPnrCancelResponseDTO>(resp, HttpStatus.REQUEST_TIMEOUT));
                unsubscribe();
            }
        });
    }

    @Override
    public void onCompleted() {
        long timeElapsed = System.currentTimeMillis() - subscriptionStartTime;
        SupplyPnrCancelResponseDTO.Builder builder = response.toBuilder();
        //builder.getMetaBuilder().setApiLatency(timeElapsed);
        response = builder.build();

        defResult.setResult(new ResponseEntity<SupplyPnrCancelResponseDTO>(response, getHttpStatusCode(response)));
        MMTLogger.info(
                (new LogParams.LogParamsBuilder()).correlationId(cancelRequest.getRequestConfig().getCorrelationId())
                        .lob(cancelRequest.getRequestConfig().getLob())
                        .serviceName(MetricServices.PNR_CANCEL_RELEASE_REQUEST_LATENCY.name()).className(PnrCancelReleaseSubscriber.class.getName())
                        .request(MMTLogger.convertToJson(cancelRequest))
                        .response(MMTLogger.convertToJson(response))
                        .extraInfo("Search Request completed").timeTaken(timeElapsed).build(),
                MetricType.LOG_FILE, MetricType.LOG_TIME);
    }

    private HttpStatus getHttpStatusCode(SupplyPnrCancelResponseDTO cancelResponse) {
        if (cancelResponse == null)
            return HttpStatus.INTERNAL_SERVER_ERROR;
        HttpStatus httpStatus = HttpStatus.OK;
        if (SupplyStatus.FAILURE.equals(cancelResponse.getCancellationStatus()) && cancelResponse.getErrCount() > 0) {
            String statusCode = cancelResponse.getErr(0).getStatusCode();
            if (String.valueOf(HttpStatus.BAD_REQUEST.value()).equals(statusCode)) {
                httpStatus = HttpStatus.BAD_REQUEST;
            } else if (String.valueOf(HttpStatus.NOT_FOUND.value()).equals(statusCode)) {
                httpStatus = HttpStatus.NOT_FOUND;
            } else if (String.valueOf(HttpStatus.FORBIDDEN.value()).equals(statusCode)) {
                httpStatus = HttpStatus.FORBIDDEN;
            } else {
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        return httpStatus;
    }

    @Override
    public void onError(Throwable e) {
        defResult.setErrorResult(new ResponseEntity<SupplyPnrCancelResponseDTO>(AdapterUtil.getErroneousResponse_CancelPnr(
                HttpStatus.INTERNAL_SERVER_ERROR, PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode(),
                PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getMessage(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR));
        MMTLogger.error(
                (new LogParams.LogParamsBuilder()).correlationId(cancelRequest.getRequestConfig().getCorrelationId())
                        .lob(cancelRequest.getRequestConfig().getLob())
                        .serviceName(MetricServices.PNR_CANCEL_RELEASE_REQUEST_ERROR.name())
                        //.tripType(cancelRequest.getRequestCore().getTripType().name())
                        .className(PnrCancelReleaseSubscriber.class.getName()).request(MMTLogger.convertToJson(cancelRequest))
                        .throwable(e).httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .errorCode(PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode()).build(),
                MetricType.LOG_FILE, MetricType.LOG_COUNTER);
    }

    @Override
    public void onNext(SupplyPnrCancelResponseDTO resp) {
        response = resp;
        if (SupplyStatus.SUCCESS.equals(resp.getCancellationStatus())) {
            MMTLogger.info(
                    (new LogParams.LogParamsBuilder())
                            .correlationId(cancelRequest.getRequestConfig().getCorrelationId())
                            .lob(cancelRequest.getRequestConfig().getLob())
                            .serviceName(MetricServices.PNR_CANCEL_RELEASE_REQUEST_SUCCESS.name())
                            //.tripType(cancelRequest.getRequestCore().getTripType().name())
                            .className(PnrCancelReleaseSubscriber.class.getName()).extraInfo("cancel release response received").build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
        }
    }
}
