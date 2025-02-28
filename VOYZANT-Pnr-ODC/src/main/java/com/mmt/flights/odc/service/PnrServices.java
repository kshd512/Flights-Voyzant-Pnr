package com.mmt.flights.odc.service;


import com.mmt.api.rxflow.FlowExecutor;
import com.mmt.api.rxflow.WorkFlow;
import com.mmt.flights.common.logging.HiveRequestResponseLogger;
import com.mmt.flights.i5.odc.service.ODCCommonFlowHandler;
import com.mmt.flights.i5.odc.service.ODCWorkflowBuilder;
import com.mmt.flights.odc.commit.DateChangeCommitRequest;
import com.mmt.flights.odc.commit.DateChangeCommitResponse;
import com.mmt.flights.odc.common.AbstractDateChangeRequest;
import com.mmt.flights.odc.common.BaseResponse;
import com.mmt.flights.odc.common.enums.Status;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentRequest;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentResponse;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import com.mmt.flights.pnr.retrieve.workflow.tasks.AirAsiaCommonLogoutTask;
import com.mmt.flights.postsales.logger.FunnelStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

@Service
public class PnrServices {

    @Autowired
    HiveRequestResponseLogger hiveLogger;

    public Observable<SimpleSearchResponseV2> odcSearchV1(DateChangeSearchRequest request) {
        return process(request, ODCWorkflowBuilder.odcSearchV1(), FunnelStep.Search, new SimpleSearchResponseV2());
    }

    public Observable<DateChangePrePaymentResponse> odcPrePayment(DateChangePrePaymentRequest request) {
        DateChangePrePaymentResponse defaultErrorResponse = new DateChangePrePaymentResponse();
        defaultErrorResponse.setStatus(Status.FAILED);
        return process(request, ODCWorkflowBuilder.odcPrePayment(), FunnelStep.PrePayment, defaultErrorResponse);
    }

    public Observable<DateChangeCommitResponse> odcCommit(DateChangeCommitRequest request) {
        DateChangeCommitResponse defaultErrorResponse = new DateChangeCommitResponse();
        defaultErrorResponse.setStatus(Status.FAILED);
        return process(request, ODCWorkflowBuilder.odcCommit(),FunnelStep.Commit, defaultErrorResponse);
    }

    public <R extends BaseResponse, Q extends AbstractDateChangeRequest> Observable<R> process(Q request, WorkFlow flow, FunnelStep operation, R defaultResponse) {
        return FlowExecutor.getBuilder(flow, new ODCCommonFlowHandler<>(hiveLogger, operation, defaultResponse,logOutTask)).build().execute(Observable.just(request));
    }
}
