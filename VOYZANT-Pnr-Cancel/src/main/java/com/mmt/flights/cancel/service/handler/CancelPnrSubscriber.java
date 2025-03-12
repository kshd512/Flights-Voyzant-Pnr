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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;
import rx.Subscriber;

import java.util.concurrent.TimeUnit;


public class CancelPnrSubscriber extends Subscriber<SupplyPnrCancelResponseDTO> {

    private final DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> defResult;
    private final SupplyPnrCancelRequestDTO request;
    private final long startTime;
    private final long serviceReplytimeout;
    private volatile SupplyPnrCancelResponseDTO response;
    private long subscriptionStartTime;

    public CancelPnrSubscriber(DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> deferredResult,
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
                SupplyPnrCancelResponseDTO resp = AdapterUtil.getErroneousResponse_CancelPnr(HttpStatus.GATEWAY_TIMEOUT,
                        PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.getCode(), PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.getMessage(),
                        PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.name());
                MMTLogger.error(
                        (new LogParams.LogParamsBuilder())
                                .correlationId(request.getRequestConfig().getCorrelationId())
                                .serviceName(MetricServices.PNR_CANCEL_TIME_OUT.name())
                                .className(this.getClass().getName())
                                .httpStatus(HttpStatus.GATEWAY_TIMEOUT.value())
                                .errorCode(PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.getCode())
                                .extraInfo("Setting Result because of Timeout in "
                                        + (System.currentTimeMillis() - startTime))
                                .build(),
                        MetricType.LOG_FILE, MetricType.LOG_COUNTER);
                defResult.setErrorResult(new ResponseEntity<SupplyPnrCancelResponseDTO>(resp, HttpStatus.GATEWAY_TIMEOUT));
                unsubscribe();
            }
        });
    }

    @Override
    public void onCompleted() {
        long timeElapsed = System.currentTimeMillis() - subscriptionStartTime;
        defResult.setResult(new ResponseEntity<>(response, getHttpStatusCode(response)));

        MMTLogger.info((new LogParams.LogParamsBuilder())
                .correlationId(request.getRequestConfig().getCorrelationId())
                .serviceName(MetricServices.PNR_CANCEL_LATENCY.name())
                .className(this.getClass().getName()).extraInfo("PNR Cancel Request completed")
                .timeTaken(timeElapsed).build(), MetricType.LOG_FILE, MetricType.LOG_TIME);
    }

    private HttpStatus getHttpStatusCode(SupplyPnrCancelResponseDTO SupplyPnrCancelResponseDTO) {
        if (response == null)
            return HttpStatus.INTERNAL_SERVER_ERROR;
        HttpStatus httpStatus = HttpStatus.OK;        // Changed to Cancellation status.
        if (SupplyStatus.FAILURE.equals(response.getCancellationStatus()) && response.getErrList() != null
                && response.getErrList().size() > 0) {
            String statusCode = response.getErrList().get(0).getStatusCode();
            httpStatus = HttpStatus.resolve(Integer.valueOf(statusCode));
        }
        return httpStatus;
    }

    @Override
    public void onError(Throwable e) {
        defResult.setErrorResult(new ResponseEntity<SupplyPnrCancelResponseDTO>(AdapterUtil.getErroneousResponse_CancelPnr(
                HttpStatus.INTERNAL_SERVER_ERROR, PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode(),
                PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getMessage(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR));
        MMTLogger.error(
                (new LogParams.LogParamsBuilder()).correlationId(request.getRequestConfig().getCorrelationId())
                        .lob(request.getRequestConfig().getLob())
                        .serviceName(MetricServices.PNR_CANCEL_REQUEST_ERROR.name())
                        .className(this.getClass().getName())
                        .supplierPnr(request.getRequestConfig().getBookingId())
                        .correlationId(request.getRequestConfig().getCorrelationId())
                        .throwable(e)
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .errorCode(PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode()).build(),
                MetricType.LOG_FILE, MetricType.LOG_COUNTER);
    }

    @Override
    public void onNext(SupplyPnrCancelResponseDTO resp) {
        response = resp;                                                    //Changed to CancellationStatus.
        if (SupplyStatus.SUCCESS.equals(resp.getCancellationStatus())) {
            MMTLogger.info((new LogParams.LogParamsBuilder())
                            .correlationId(request.getRequestConfig().getCorrelationId())
                            .lob(request.getRequestConfig().getLob())
                            //	.itineraryId(request.getRequestConfig().getItineraryId())
                            .supplierPnr(request.getRequestConfig().getBookingId())
                            .correlationId(request.getRequestConfig().getCorrelationId())
                            .serviceName(MetricServices.PNR_CANCEL_REQUEST_SUCCESS.name())
                            .className(this.getClass().getName()).extraInfo("Cancel PNR response received").build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
        }
    }
}
