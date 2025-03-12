package com.mmt.flights.pnr.service.handler;

import com.mmt.api.rxflow.FlowHandler;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.common.util.AdapterUtil;
import com.mmt.flights.postsales.error.PSErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.postsales.logger.FunnelStep;
import com.mmt.flights.supply.book.v4.response.SupplyBookingResponseDTO;
import com.mmt.flights.supply.common.SupplyErrorDetailDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import com.mmt.flights.supply.pnr.v4.request.SupplyPnrRequestDTO;

import java.util.HashMap;

public class PnrFlowHandler implements FlowHandler<SupplyPnrRequestDTO, SupplyBookingResponseDTO> {

    private final String lob;
    private final String src;

    public PnrFlowHandler(String lob, String src) {
        this.lob = lob;
        this.src = src;
    }

    public HashMap<String, Object> startAdapter(SupplyPnrRequestDTO request) {
        HashMap<String, Object> newMap = new HashMap<>();

        newMap.put(FlowStateKey.REQUEST, request);
        newMap.put(FlowStateKey.PNR_RETRIEVE_REQUEST, request);
        newMap.put(FlowStateKey.SUPPLIER_PNR, request.getSupplierPnr());
        newMap.put(FlowStateKey.LOG_KEY, request.getSupplierPnr());
        newMap.put(FlowStateKey.LOB, lob);
        newMap.put(FlowStateKey.SOURCE, src);
        newMap.put(FlowStateKey.FUNNEL_STEP, FunnelStep.Get);

        newMap.put(FlowStateKey.CMS_ID, request.getCredentialId());
        return newMap;
    }

    public SupplyBookingResponseDTO successStateAdapter(FlowState resultState) {
        return resultState.getValue(FlowStateKey.RESPONSE);
    }

    public SupplyBookingResponseDTO failureStateHandler(FlowState state) {
        Throwable t = state.getError();
        SupplyBookingResponseDTO.Builder response = SupplyBookingResponseDTO.newBuilder();
        response.setStatus(SupplyStatus.FAILURE);
        PSErrorEnum errorCode = com.mmt.flights.postsales.error.PSCommonErrorEnum.FLT_UNKNOWN_ERROR;
        SupplyErrorDetailDTO downStreamError = null;
        if (t instanceof PSErrorException) {
            PSErrorException fce = (PSErrorException) t;
            if (fce.getPsErrorEnum() != null) {
                errorCode = fce.getPsErrorEnum();
            }
        }
        SupplyErrorDetailDTO error = AdapterUtil.getSupplyErrorDetail(errorCode, t.getMessage(), downStreamError);
        response.addErr(error);
        SupplyBookingResponseDTO finalResponse = response.build();
        MMTLogger.error(
                (new LogParams.LogParamsBuilder())
                        .lobSrcAndLogKey(state)
                        .serviceName(AdapterUtil.getMetricServiceName(MetricServices.PNR_RETRIEVE_REQUEST_ERROR.name(), errorCode))
                        .throwable(t)
                        .response(MMTLogger.convertProtoToJson(finalResponse))
                        .className(this.getClass().getName())
                        .build(),
                MetricType.LOG_FILE, MetricType.LOG_COUNTER);
        return finalResponse;
    }


}
