package com.mmt.flights.controllers;

import com.mmt.flights.common.config.TechConfig;
import com.mmt.flights.common.constants.CommonConstants;
import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.common.util.AdapterUtil;
import com.mmt.flights.constants.EndpointConstants;
import com.mmt.flights.pnr.service.RetrievePnrService;
import com.mmt.flights.pnr.service.RetrievePnrSubscriber;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.supply.book.v4.response.SupplyBookingResponseDTO;
import com.mmt.flights.supply.pnr.v4.request.SupplyPnrRequestDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;

@RestController
@Api("VOYZANT PNR Controller")
public class RetrievePnrController {
    
    @Autowired
    private RetrievePnrService pnrService;

    @Autowired
    private TechConfig techConfig;

    @RequestMapping(value = EndpointConstants.RETRIEVE_PNR, method = RequestMethod.POST)
    @ApiOperation(value = "VOYZANT PNR API", response = SupplyBookingResponseDTO.class, notes = "This API pnr with VOYZANT connector")
    public DeferredResult<ResponseEntity<SupplyBookingResponseDTO>> retrievePnrV1(
            @RequestBody @ApiParam(value = "Pnr Retrieve Request", required = true) SupplyPnrRequestDTO retrieveRequest,
            @RequestHeader(name = "x-lob", required = false) String lob,
            @RequestHeader(name = "x-src", required = false) String src) {

        return retrievePnrCommon(retrieveRequest, lob, src, CommonConstants.VERSION_V1);
    }

    public DeferredResult<ResponseEntity<SupplyBookingResponseDTO>> retrievePnrCommon(SupplyPnrRequestDTO retrieveRequest, String lob, String src, String version){
        long startTime = System.currentTimeMillis();
        long timeout = techConfig.getpnrRetrieveTimeout();
        DeferredResult<ResponseEntity<SupplyBookingResponseDTO>> deferredResult = new DeferredResult<>(timeout);
        try {
            MMTLogger.info(
                    (new LogParams.LogParamsBuilder())
                            .correlationId(retrieveRequest.getSupplierPnr())
                            .serviceName(MetricServices.PNR_RETRIEVE_REQUEST_TOTAL.name())
                            .className(this.getClass().getName())
                            .extraInfo("Api version: " + version)
                            .request(MMTLogger.convertProtoToJson(retrieveRequest)).build(),
                    MetricType.LOG_FILE, MetricType.LOG_COUNTER);
            Observable<SupplyBookingResponseDTO> observableResponse = pnrService.retrievePnr(retrieveRequest,lob,src,version);
            observableResponse
                    .subscribe(new RetrievePnrSubscriber(deferredResult, retrieveRequest, startTime, timeout));

        } catch (Exception e) {
            MMTLogger.error((new LogParams.LogParamsBuilder())
                            .correlationId(retrieveRequest.getSupplierPnr())
                            .serviceName(MetricServices.PNR_RETRIEVE_REQUEST_ERROR.name()).className(this.getClass().getName())
                            .request(MMTLogger.convertProtoToJson(retrieveRequest)).throwable(e)
                            .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .errorCode(PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode()).build(), MetricType.LOG_FILE,
                    MetricType.LOG_COUNTER);
            deferredResult.setErrorResult(new ResponseEntity<>(AdapterUtil.getErroneousResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR, PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getCode(), e.getMessage(),
                    PSCommonErrorEnum.FLT_UNKNOWN_ERROR.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR));
        }

        return deferredResult;
    }

}
