package com.mmt.flights.odc.service.handler;

import com.mmt.api.rxflow.FlowHandler;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.HiveRequestResponseLogger;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentRequest;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentResponse;
import com.mmt.flights.postsales.logger.FunnelStep;

import java.util.HashMap;

public class ODCPrePaymentFlowHandler implements FlowHandler<DateChangePrePaymentRequest, DateChangePrePaymentResponse> {

    private final HiveRequestResponseLogger hiveLogger;
    private final String api;
    private final FunnelStep funnelStep;

    public ODCPrePaymentFlowHandler(HiveRequestResponseLogger logger){
        hiveLogger = logger;
        this.funnelStep = FunnelStep.Search;
        api = getApi();
    }

    @Override
    public HashMap<String, Object> startAdapter(DateChangePrePaymentRequest request) {
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put(FlowStateKey.REQUEST, request);
        newMap.put(FlowStateKey.SUPPLIER_PNR, request.getPnr());
        newMap.put(FlowStateKey.LOG_KEY, request.getPnr());
        newMap.put(FlowStateKey.LOB, request.getLob());
        newMap.put(FlowStateKey.SOURCE, request.getSrc());
        newMap.put(FlowStateKey.CMS_ID, request.getCmsId());
        newMap.put(FlowStateKey.AIRLINE,request.getAirline());
        newMap.put(FlowStateKey.FUNNEL_STEP,funnelStep);
        return newMap;
    }

    @Override
    public DateChangePrePaymentResponse successStateAdapter(FlowState flowState) {
        return flowState.getValue(FlowStateKey.RESPONSE);
    }

    @Override
    public DateChangePrePaymentResponse failureStateHandler(FlowState state) {
        return null;
    }

    String getErrorMetric(){
        return null;
    }

    private String getApi(){
        return null;
    }
}
