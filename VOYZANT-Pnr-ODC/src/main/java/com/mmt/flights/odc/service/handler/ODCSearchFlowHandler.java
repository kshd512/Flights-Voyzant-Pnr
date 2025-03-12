package com.mmt.flights.odc.service.handler;

import com.mmt.api.rxflow.FlowHandler;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.HiveRequestResponseLogger;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import com.mmt.flights.postsales.logger.FunnelStep;

import java.util.HashMap;

public class ODCSearchFlowHandler implements FlowHandler<DateChangeSearchRequest, SimpleSearchResponseV2> {

    private final HiveRequestResponseLogger hiveLogger;
    private final String api;
    private final FunnelStep funnelStep;
    public ODCSearchFlowHandler(HiveRequestResponseLogger logger){
        hiveLogger = logger;
        this.funnelStep = FunnelStep.Search;
        api = getApi();
    }

    @Override
    public HashMap<String, Object> startAdapter(DateChangeSearchRequest request) {
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
    public SimpleSearchResponseV2 successStateAdapter(FlowState flowState) {
        return flowState.getValue(FlowStateKey.RESPONSE);
    }

    @Override
    public SimpleSearchResponseV2 failureStateHandler(FlowState state) {
        return null;
    }

     String getErrorMetric(){
         return null;
     }

    private String getApi(){
        return null;
    }
}
