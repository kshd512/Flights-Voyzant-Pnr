package com.mmt.flights.odc.service.handler;

import com.mmt.api.rxflow.FlowHandler;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.HiveRequestResponseLogger;
import com.mmt.flights.odc.commit.DateChangeCommitRequest;
import com.mmt.flights.odc.commit.DateChangeCommitResponse;
import com.mmt.flights.postsales.logger.FunnelStep;

import java.util.HashMap;

public class ODCCommitFlowHandler implements FlowHandler<DateChangeCommitRequest, DateChangeCommitResponse> {

    private final HiveRequestResponseLogger hiveLogger;
    private final String api;
    private final FunnelStep funnelStep;

    public ODCCommitFlowHandler(HiveRequestResponseLogger logger){
        hiveLogger = logger;
        this.funnelStep = FunnelStep.Search;
        api = getApi();
    }

    @Override
    public HashMap<String, Object> startAdapter(DateChangeCommitRequest request) {
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
    public DateChangeCommitResponse successStateAdapter(FlowState flowState) {
        return flowState.getValue(FlowStateKey.RESPONSE);
    }

    @Override
    public DateChangeCommitResponse failureStateHandler(FlowState state) {
        return null;
    }

    String getErrorMetric(){
        return null;
    }

    private String getApi(){
        return null;
    }
}
