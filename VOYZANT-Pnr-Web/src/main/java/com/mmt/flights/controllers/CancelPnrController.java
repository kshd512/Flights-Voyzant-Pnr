package com.mmt.flights.controllers;

import com.mmt.flights.cancel.service.CancelPnrService;
import com.mmt.flights.cancel.service.CancelPnrSubscriber;
import com.mmt.flights.cancel.service.ValidateCancelSubscriber;
import com.mmt.flights.common.config.TechConfig;
import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.common.util.AdapterUtil;
import com.mmt.flights.constants.EndpointConstants;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyValidateCancelResponseDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;

@RestController
@Api("VOYZANT PNR Controller")
public class CancelPnrController {

    @Autowired
    private TechConfig techConfig;

    @Autowired
    private CancelPnrService cancelPnrService;

    @RequestMapping(value = EndpointConstants.VALIDATE_CANCEL_V1, method = RequestMethod.POST)
    @ApiOperation(value = "VOYZANT cancel validate API", response = SupplyValidateCancelResponseDTO.class, notes = "This API pnr with VOYZANT connector")
    public DeferredResult<ResponseEntity<SupplyValidateCancelResponseDTO>> validateCancel(
            @RequestBody SupplyPnrCancelRequestDTO request) {
        long startTime = System.currentTimeMillis();
        long timeout = techConfig.getPnrCancelTimeout();
        DeferredResult<ResponseEntity<SupplyValidateCancelResponseDTO>> deferredResult = new DeferredResult<>(timeout);
        try {
            String logKey = request.getRequestConfig().getCorrelationId();
            MMTLogger.info(
                    (new LogParams.LogParamsBuilder())
                            .correlationId(logKey)
                            .lob(request.getRequestConfig().getLob())
                            .src(request.getRequestConfig().getSource())
                            .className(this.getClass().getName())
                            .extraInfo("Cancellation validation request")
                            .serviceName(MetricServices.PNR_CANCEL_REQUEST_COUNTER.name())
                            .request(MMTLogger.convertProtoToJson(request))
                            .build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
            Observable<SupplyValidateCancelResponseDTO> observableResponse = cancelPnrService.validateCancelPnr(request);
            observableResponse
                    .subscribe(new ValidateCancelSubscriber(deferredResult, request, startTime, timeout));

        } catch (Exception e) {
            MMTLogger.error((new LogParams.LogParamsBuilder())
                            .serviceName(MetricServices.VALIDATE_CANCEL_REQUEST_ERROR.name())
                            .className(this.getClass().getName())
                            .src(request.getRequestConfig().getSource()).lob(request.getRequestConfig().getLob())
                            .request(MMTLogger.convertProtoToJson(request)).throwable(e)
                            .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .errorCode(PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode()).build(), MetricType.LOG_FILE,
                    MetricType.LOG_COUNTER);
            deferredResult.setErrorResult(new ResponseEntity<>(AdapterUtil.getErroneousResponseValidateCancel(
                    HttpStatus.INTERNAL_SERVER_ERROR, PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode(), e.getMessage(),
                    PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR));
        }
        return deferredResult;
    }

    @RequestMapping(value = EndpointConstants.CANCEL_PNR_V1, method = RequestMethod.POST)
    @ApiOperation(value = "VOYZANT cancel validate API", response = SupplyValidateCancelResponseDTO.class, notes = "This API pnr with VOYZANT connector")
    public DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> cancelPnr(
            @RequestBody SupplyPnrCancelRequestDTO request) {
        long startTime = System.currentTimeMillis();
        long timeout = techConfig.getPnrCancelTimeout();
        DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> deferredResult = new DeferredResult<>(timeout);
        try {
            String logKey = request.getRequestConfig().getCorrelationId();
            MMTLogger.info(
                    (new LogParams.LogParamsBuilder())
                            .correlationId(logKey)
                            .lob(request.getRequestConfig().getLob())
                            .src(request.getRequestConfig().getSource())
                            .className(this.getClass().getName())
                            .extraInfo("Cancellation validation request")
                            .serviceName(MetricServices.PNR_CANCEL_REQUEST_COUNTER.name())
                            .request(MMTLogger.convertProtoToJson(request))
                            .build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
            Observable<SupplyPnrCancelResponseDTO> observableResponse = null;
            observableResponse = cancelPnrService.cancelPnr(request);
            observableResponse
                    .subscribe(new CancelPnrSubscriber(deferredResult, request, startTime, timeout));

        } catch (Exception e) {
            MMTLogger.error((new LogParams.LogParamsBuilder())
                            .serviceName(MetricServices.PNR_CANCEL_REQUEST_ERROR.name())
                            .className(this.getClass().getName())
                            .src(request.getRequestConfig().getSource()).lob(request.getRequestConfig().getLob())
                            .request(MMTLogger.convertProtoToJson(request)).throwable(e)
                            .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .errorCode(PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode()).build(), MetricType.LOG_FILE,
                    MetricType.LOG_COUNTER);
            deferredResult.setErrorResult(new ResponseEntity<>(AdapterUtil.getErroneousResponseValidateCancel(
                    HttpStatus.INTERNAL_SERVER_ERROR, PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode(), e.getMessage(),
                    PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR));
        }
        return deferredResult;
    }
}
