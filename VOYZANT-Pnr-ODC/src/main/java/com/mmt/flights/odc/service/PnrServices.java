package com.mmt.flights.odc.service;


import com.mmt.api.rxflow.FlowExecutor;
import com.mmt.flights.common.logging.HiveRequestResponseLogger;
import com.mmt.flights.odc.commit.DateChangeCommitRequest;
import com.mmt.flights.odc.commit.DateChangeCommitResponse;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentRequest;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentResponse;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

@Service
public class PnrServices {

    @Autowired
    HiveRequestResponseLogger hiveLogger;

    public Observable<SimpleSearchResponseV2> odcSearch(DateChangeSearchRequest request) {
        return FlowExecutor.getBuilder(ODCWorkflowBuilder.odcSearch(), new ODCSearchFlowHandler(hiveLogger)).build().execute(Observable.just(request));
    }

    public Observable<DateChangePrePaymentResponse> odcPrePayment(DateChangePrePaymentRequest request) {
        return FlowExecutor.getBuilder(ODCWorkflowBuilder.odcPrePayment(), new ODCPrePaymentFlowHandler(hiveLogger)).build().execute(Observable.just(request));
    }

    public Observable<DateChangeCommitResponse> odcCommit(DateChangeCommitRequest request) {
        return FlowExecutor.getBuilder(ODCWorkflowBuilder.odcCommit(), new ODCCommitFlowHandler(hiveLogger)).build().execute(Observable.just(request));
    }
}
