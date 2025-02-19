package com.mmt.flights.pnr.service;

import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.common.util.AdapterUtil;
import com.mmt.flights.common.util.IPAddressUtil;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.supply.book.v4.response.SupplyBookingResponseDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import com.mmt.flights.supply.pnr.v4.request.SupplyPnrRequestDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;
import rx.Subscriber;

import java.util.concurrent.TimeUnit;

public class RetrievePnrSubscriber extends Subscriber<SupplyBookingResponseDTO> {


    private final DeferredResult<ResponseEntity<SupplyBookingResponseDTO>> defResult;
    private final SupplyPnrRequestDTO request;
    private final long startTime;
    private final long serviceReplytimeout;
    private volatile SupplyBookingResponseDTO response;
    private long subscriptionStartTime;

    public RetrievePnrSubscriber(DeferredResult<ResponseEntity<SupplyBookingResponseDTO>> deferredResult,
                                 SupplyPnrRequestDTO request, long startTime, long serviceReplytimeout) {
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
                SupplyBookingResponseDTO resp = AdapterUtil.getErroneousResponse(HttpStatus.GATEWAY_TIMEOUT,
                        com.mmt.flights.postsales.error.PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.getCode(), com.mmt.flights.postsales.error.PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.getMessage(),
                        com.mmt.flights.postsales.error.PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.name());
                MMTLogger.error(
                        (new LogParams.LogParamsBuilder())
                                .correlationId(request.getSupplierPnr())
                                .serviceName(MetricServices.PNR_RETRIEVE_TIME_OUT.name())
                                .className(this.getClass().getName())
                                .request(MMTLogger.convertProtoToJson(request))
                                .httpStatus(HttpStatus.GATEWAY_TIMEOUT.value())
                                .errorCode(com.mmt.flights.postsales.error.PSCommonErrorEnum.EXT_SERVICE_TIMED_OUT.getCode())
                                .extraInfo("Setting Result because of Timeout in "
                                        + (System.currentTimeMillis() - startTime))
                                .build(),
                        MetricType.LOG_FILE, MetricType.LOG_COUNTER);
                defResult.setErrorResult(new ResponseEntity<>(resp, HttpStatus.GATEWAY_TIMEOUT));
                unsubscribe();
            }
        });
    }

    @Override
    public void onCompleted() {
        long timeElapsed = System.currentTimeMillis() - subscriptionStartTime;
        SupplyBookingResponseDTO.Builder builder = response.toBuilder();
        builder.getMetaDataBuilder().setApiLatency(timeElapsed).setIpAddress(IPAddressUtil.getIPAddress());
        response = builder.build();
        defResult.setResult(new ResponseEntity<>(response, getHttpStatusCode(response)));

        MMTLogger.info((new LogParams.LogParamsBuilder())
                .correlationId(request.getSupplierPnr())
                .serviceName(MetricServices.PNR_RETRIEVE_LATENCY.name())
                .className(this.getClass().getName()).extraInfo("PNR Retrieve Request completed")
                .timeTaken(timeElapsed).build(), MetricType.LOG_FILE, MetricType.LOG_TIME);
    }

    private HttpStatus getHttpStatusCode(SupplyBookingResponseDTO response) {
        if (response == null)
            return HttpStatus.INTERNAL_SERVER_ERROR;
        HttpStatus httpStatus = HttpStatus.OK;
        if (SupplyStatus.FAILURE.equals(response.getStatus()) && response.getErrList() != null
                && response.getErrList().size() > 0) {
            String statusCode = response.getErr(0).getStatusCode();
            if(StringUtils.isNotBlank(statusCode))
                httpStatus = HttpStatus.resolve(Integer.parseInt(statusCode));
            else
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return httpStatus;
    }

    @Override
    public void onError(Throwable e) {
        defResult.setErrorResult(new ResponseEntity<>(AdapterUtil.getErroneousResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode(),
                PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getMessage(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR));
        MMTLogger.error(
                (new LogParams.LogParamsBuilder()).correlationId(request.getSupplierPnr())
                        .serviceName(MetricServices.PNR_RETRIEVE_REQUEST_ERROR.name())
                        .className(this.getClass().getName())
                        .request(MMTLogger.convertProtoToJson(request)).throwable(e)
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .errorCode(PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode()).build(),
                MetricType.LOG_FILE, MetricType.LOG_COUNTER);
    }

    @Override
    public void onNext(SupplyBookingResponseDTO resp) {
        response = resp;
        if (SupplyStatus.SUCCESS.equals(resp.getStatus())) {
            MMTLogger.info((new LogParams.LogParamsBuilder())
                            .correlationId(request.getSupplierPnr())
                            .serviceName(MetricServices.PNR_RETRIEVE_REQUEST_SUCCESS.name())
                            .className(this.getClass().getName()).extraInfo("PNR response received").build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
        }
    }
}
