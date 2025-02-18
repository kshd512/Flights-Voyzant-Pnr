package com.mmt.flights.controllers;

import com.mmt.flights.cancel.service.CancelPnrService;
import com.mmt.flights.cancel.service.CancelPnrSubscriber;
import com.mmt.flights.cancel.service.PnrCancelReleaseSubscriber;
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
import io.swagger.annotations.ApiParam;
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
        return validateCancelPnr(request);
    }

    public DeferredResult<ResponseEntity<SupplyValidateCancelResponseDTO>> validateCancelPnr(SupplyPnrCancelRequestDTO cancelRequest) {
        long startTime = System.currentTimeMillis();
        long timeout = techConfig.getPnrCancelTimeout();
        DeferredResult<ResponseEntity<SupplyValidateCancelResponseDTO>> deferredResult = new DeferredResult<>(timeout);
        try {
            String logKey = cancelRequest.getRequestConfig().getCorrelationId();
            MMTLogger.info(
                    (new LogParams.LogParamsBuilder())
                            .correlationId(logKey)
                            .lob(cancelRequest.getRequestConfig().getLob())
                            .src(cancelRequest.getRequestConfig().getSource())
                            .className(this.getClass().getName())
                            .extraInfo("Cancellation validation request")
                            .serviceName(MetricServices.PNR_CANCEL_REQUEST_COUNTER.name())
                            .request(MMTLogger.convertProtoToJson(cancelRequest))
                            .build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
            Observable<SupplyValidateCancelResponseDTO> observableResponse = cancelPnrService.validateCancelPnr(cancelRequest);
            observableResponse
                    .subscribe(new ValidateCancelSubscriber(deferredResult, cancelRequest, startTime, timeout));

        } catch (Exception e) {
            MMTLogger.error((new LogParams.LogParamsBuilder())
                            .serviceName(MetricServices.VALIDATE_CANCEL_REQUEST_ERROR.name())
                            .className(this.getClass().getName())
                            .src(cancelRequest.getRequestConfig().getSource()).lob(cancelRequest.getRequestConfig().getLob())
                            .request(MMTLogger.convertProtoToJson(cancelRequest)).throwable(e)
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
    public DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> cancelPnrV1(
            @RequestBody SupplyPnrCancelRequestDTO request) {
        return cancelPnr(request);
    }

    public DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> cancelPnr(SupplyPnrCancelRequestDTO cancelRequest) {
        long startTime = System.currentTimeMillis();
        long timeout = techConfig.getPnrCancelTimeout();
        DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> deferredResult = new DeferredResult<>(timeout);
        try {
            String logKey = cancelRequest.getRequestConfig().getCorrelationId();
            MMTLogger.info(
                    (new LogParams.LogParamsBuilder())
                            .correlationId(logKey)
                            .lob(cancelRequest.getRequestConfig().getLob())
                            .src(cancelRequest.getRequestConfig().getSource())
                            .className(this.getClass().getName())
                            .extraInfo("Cancellation validation request")
                            .serviceName(MetricServices.PNR_CANCEL_REQUEST_COUNTER.name())
                            .request(MMTLogger.convertProtoToJson(cancelRequest))
                            .build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
            Observable<SupplyPnrCancelResponseDTO> observableResponse = cancelPnrService.cancelPnr(cancelRequest);
            observableResponse
                    .subscribe(new CancelPnrSubscriber(deferredResult, cancelRequest, startTime, timeout));

        } catch (Exception e) {
            MMTLogger.error((new LogParams.LogParamsBuilder())
                            .serviceName(MetricServices.PNR_CANCEL_REQUEST_ERROR.name())
                            .className(this.getClass().getName())
                            .src(cancelRequest.getRequestConfig().getSource()).lob(cancelRequest.getRequestConfig().getLob())
                            .request(MMTLogger.convertProtoToJson(cancelRequest)).throwable(e)
                            .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .errorCode(PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode()).build(), MetricType.LOG_FILE,
                    MetricType.LOG_COUNTER);
            deferredResult.setErrorResult(new ResponseEntity<>(AdapterUtil.getErroneousResponseValidateCancel(
                    HttpStatus.INTERNAL_SERVER_ERROR, PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode(), e.getMessage(),
                    PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR));
        }
        return deferredResult;
    }


    @RequestMapping(value = EndpointConstants.VOID_CANCEL, method = RequestMethod.POST)
    @ApiOperation(value = "VOYZANT cancel validate API", response = SupplyValidateCancelResponseDTO.class, notes = "This API pnr with VOYZANT connector")
    public DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> voidCancelPnrV1(
            @RequestBody SupplyPnrCancelRequestDTO request) {
        return voidCancelPnr(request);
    }

    public DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> voidCancelPnr(SupplyPnrCancelRequestDTO cancelRequest) {
        long startTime = System.currentTimeMillis();
        long timeout = techConfig.getPnrCancelTimeout();
        DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> deferredResult = new DeferredResult<>(timeout);
        try {
            String logKey = cancelRequest.getRequestConfig().getCorrelationId();
            MMTLogger.info(
                    (new LogParams.LogParamsBuilder())
                            .correlationId(logKey)
                            .lob(cancelRequest.getRequestConfig().getLob())
                            .src(cancelRequest.getRequestConfig().getSource())
                            .className(this.getClass().getName())
                            .extraInfo("VOID Cancellation validation request")
                            .serviceName(MetricServices.VOID_PNR_CANCEL_REQUEST_COUNTER.name())
                            .request(MMTLogger.convertProtoToJson(cancelRequest))
                            .build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
            Observable<SupplyPnrCancelResponseDTO> observableResponse = cancelPnrService.voidCancelPnr(cancelRequest);
            observableResponse
                    .subscribe(new CancelPnrSubscriber(deferredResult, cancelRequest, startTime, timeout));

        } catch (Exception e) {
            MMTLogger.error((new LogParams.LogParamsBuilder())
                            .serviceName(MetricServices.PNR_CANCEL_REQUEST_ERROR.name())
                            .className(this.getClass().getName())
                            .src(cancelRequest.getRequestConfig().getSource()).lob(cancelRequest.getRequestConfig().getLob())
                            .request(MMTLogger.convertProtoToJson(cancelRequest)).throwable(e)
                            .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .errorCode(PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode()).build(), MetricType.LOG_FILE,
                    MetricType.LOG_COUNTER);
            deferredResult.setErrorResult(new ResponseEntity<>(AdapterUtil.getErroneousResponseValidateCancel(
                    HttpStatus.INTERNAL_SERVER_ERROR, PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode(), e.getMessage(),
                    PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR));
        }
        return deferredResult;
    }

    @RequestMapping(value = EndpointConstants.PNR_CANCEL_RELEASE, method = RequestMethod.POST)
    @ApiOperation(value = "VOYZANT PNR Cancel Release API", response = SupplyPnrCancelResponseDTO.class, notes = "This API cancels pnr and release segments with VOYZANT connector")
    public DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> cancelReleasePnr(
            @RequestBody @ApiParam(value = "Pnr Cancel Request", required = true) SupplyPnrCancelRequestDTO cancelRequest) {

        long timeout = techConfig.getPnrCancelTimeout();
        DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>> deferredResult = new DeferredResult<ResponseEntity<SupplyPnrCancelResponseDTO>>(timeout);
        try {
            MMTLogger.info((new LogParams.LogParamsBuilder())
                    .correlationId(cancelRequest.getRequestConfig().getCorrelationId())
                    .lob(cancelRequest.getRequestConfig().getLob())
                    .serviceName(MetricServices.PNR_CANCEL_RELEASE_REQUEST_TOTAL.name())
                    .className(CancelPnrController.class.getName()).request(MMTLogger.convertProtoToJson(cancelRequest))
                    .build(), MetricType.LOG_FILE, MetricType.LOG_COUNTER);

            long startTime = System.currentTimeMillis();

            Observable<SupplyPnrCancelResponseDTO> observableResponse = cancelPnrService.cancelReleasePnr(cancelRequest);
            observableResponse.subscribe(
                    new PnrCancelReleaseSubscriber(deferredResult, cancelRequest, startTime, timeout));
        } catch (Exception e) {
            MMTLogger.error(
                    (new LogParams.LogParamsBuilder())
                            .correlationId(cancelRequest.getRequestConfig().getCorrelationId())
                            .lob(cancelRequest.getRequestConfig().getLob())
                            .serviceName(MetricServices.PNR_CANCEL_RELEASE_REQUEST_ERROR.name())
                            .className(CancelPnrController.class.getName()).request(MMTLogger.convertProtoToJson(cancelRequest))
                            .throwable(e).httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .errorCode(PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode()).build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
            deferredResult.setErrorResult(new ResponseEntity<>(AdapterUtil.getErroneousResponseValidateCancel(
                    HttpStatus.INTERNAL_SERVER_ERROR, PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode(), e.getMessage(),
                    PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR));
        }

        return deferredResult;
    }
}
