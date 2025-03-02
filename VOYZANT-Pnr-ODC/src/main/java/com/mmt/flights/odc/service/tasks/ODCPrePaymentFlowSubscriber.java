package com.mmt.flights.odc.service.tasks;

import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.odc.common.AbstractDateChangeRequest;
import com.mmt.flights.odc.constant.RequestType;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentResponse;
import com.mmt.flights.odc.service.ODCSearchFlowSubscriber;
import com.mmt.flights.odc.util.ODCUtil;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import com.mmt.flights.postsales.error.PSErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;
import rx.Subscriber;

import java.util.concurrent.TimeUnit;

public class ODCPrePaymentFlowSubscriber extends Subscriber<DateChangePrePaymentResponse> {

    private final DeferredResult<ResponseEntity<DateChangePrePaymentResponse>> defResult;
    private final AbstractDateChangeRequest request;
    private volatile DateChangePrePaymentResponse response;
    private long subscriptionStartTime;
    private final long startTime;
    private final long serviceReplyTimeout;
    private final RequestType operation;

    public ODCPrePaymentFlowSubscriber(DeferredResult<ResponseEntity<DateChangePrePaymentResponse>> deferredResult,
                                   AbstractDateChangeRequest request, long startTime,
                                   long serviceReplyTimeout, RequestType operation) {
        this.defResult = deferredResult;
        this.request = request;
        this.startTime = startTime;
        this.serviceReplyTimeout = serviceReplyTimeout;
        this.operation = operation;
    }

    @Override
    public void onStart() {
        this.subscriptionStartTime = System.currentTimeMillis();
        long timeElapsedSinceStarted = this.subscriptionStartTime - startTime;
        long timeout = serviceReplyTimeout - timeElapsedSinceStarted;
        Observable.timer(timeout, TimeUnit.MILLISECONDS).subscribe(item1 -> {
            if (!defResult.isSetOrExpired()) {
                SimpleSearchResponseV2 resp = new SimpleSearchResponseV2();
                resp.setError(ODCUtil.getErrorDetails(ErrorEnum.EXT_SERVICE_TIMED_OUT,
                        "Service timed out after " + timeout + " ms"));

                MMTLogger.error(
                        (new LogParams.LogParamsBuilder())
                                .serviceName(operation.name() + "_TIMEOUT")
                                .className(ODCSearchFlowSubscriber.class.getName())
                                .src(request.getSrc()).lob(request.getLob())
                                .correlationId(request.getPnr())
                                .httpStatus(HttpStatus.GATEWAY_TIMEOUT.value())
                                .errorCode(ErrorEnum.EXT_SERVICE_TIMED_OUT.getCode())
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
        defResult.setResult(new ResponseEntity<>(response, getHttpStatusCode()));
        MMTLogger.info((new LogParams.LogParamsBuilder())
                        .serviceName(operation.name() + "_LATENCY")
                        .className(ODCSearchFlowSubscriber.class.getName())
                        .extraInfo("ODC Request completed")
                        .src(request.getSrc()).lob(request.getLob())
                        .correlationId(request.getPnr())
                        .timeTaken(timeElapsed).build(),
                MetricType.LOG_FILE, MetricType.LOG_TIME);
    }

    private HttpStatus getHttpStatusCode() {
        if (response == null)
            return HttpStatus.INTERNAL_SERVER_ERROR;

        if (response.getError() != null) {
            String statusCode = response.getError().getErrorCode();
            try {
                return HttpStatus.resolve(Integer.parseInt(statusCode));
            } catch (NumberFormatException e) {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        return HttpStatus.OK;
    }

    @Override
    public void onError(Throwable e) {
        SimpleSearchResponseV2 errorResponse = new SimpleSearchResponseV2();
        ErrorEnum errorCode = ErrorEnum.FLT_UNKNOWN_ERROR;

        if (e instanceof PSErrorException) {
            PSErrorEnum psError = ((PSErrorException) e).getPsErrorEnum();
            if (psError instanceof ErrorEnum) {
                errorCode = (ErrorEnum) psError;
            }
        }

        errorResponse.setError(ODCUtil.getErrorDetails(errorCode, e.getMessage()));

        defResult.setErrorResult(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));

        MMTLogger.error(
                (new LogParams.LogParamsBuilder())
                        .serviceName(operation.name() + "_REQUEST_ERROR")
                        .className(ODCSearchFlowSubscriber.class.getName())
                        .src(request.getSrc()).lob(request.getLob())
                        .correlationId(request.getPnr()).throwable(e)
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .errorCode(errorCode.getCode()).build(),
                MetricType.LOG_FILE, MetricType.LOG_COUNTER);
    }

    @Override
    public void onNext(DateChangePrePaymentResponse resp) {
        response = resp;
        if (resp != null && resp.getError() == null) {
            MMTLogger.info((new LogParams.LogParamsBuilder())
                            .serviceName(operation.name() + "_REQUEST_SUCCESS")
                            .src(request.getSrc()).lob(request.getLob())
                            .correlationId(request.getPnr())
                            .className(ODCSearchFlowSubscriber.class.getName())
                            .extraInfo("ODC response received").build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
        }
    }
}
