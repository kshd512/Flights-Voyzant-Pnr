package com.mmt.flights.odc.service;


import com.mmt.api.rxflow.FlowExecutor;
import com.mmt.flights.cancel.workflow.CancellationValidationHandler;
import com.mmt.flights.common.logging.HiveRequestResponseLogger;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import com.mmt.flights.postsales.logger.FunnelStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

@Service
public class PnrServices {

    @Autowired
    HiveRequestResponseLogger hiveLogger;

    public Observable<SimpleSearchResponseV2> odcSearch(DateChangeSearchRequest request) {
        return FlowExecutor.getBuilder(ODCWorkflowBuilder.odcSearchV1(), new ODCCommonFlowHandler(hiveLogger)).build().execute(Observable.just(request));
    }
}
