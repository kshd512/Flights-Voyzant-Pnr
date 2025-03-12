package com.mmt.flights.cancel.service.handler;

import com.mmt.api.rxflow.WorkFlow;
import com.mmt.flights.cancel.service.tasks.*;
import com.mmt.flights.pnr.service.tasks.CMSManagerTask;
import com.mmt.flights.pnr.service.tasks.DummyTask;
import com.mmt.flights.pnr.service.tasks.PnrRetrieveNetworkCall;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.stereotype.Component;

import static com.mmt.api.rxflow.rule.Rules.completeFlow;
import static com.mmt.api.rxflow.rule.Rules.retry;

@Component
public class CancelPnrWorkflowBuilder {

    public static WorkFlow validateCancelPnr() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(CancelPnrRetrieveRequestAdapter.class)
                .toMap(PnrRetrieveNetworkCall.class, retry(2).onError(CancelPnrWorkflowBuilder::retryable))
                .toMap(ValidateCancelPnrTask.class)
                .toMap(CancelPnrResponseAdaptor.class, completeFlow()).build();
    }

    public static WorkFlow cancelPnr() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(CancelPnrRetrieveRequestAdapter.class)
                .toMap(PnrRetrieveNetworkCall.class, retry(2).onError(CancelPnrWorkflowBuilder::retryable))
                .toMap(ValidateCancelPnrTask.class)
                .toMap(CancelPnrRequestAdapterTask.class)
                .toMap(CancelPnrNetworkCallTask.class)
                .toMap(CancelPnrResponseAdaptor.class, completeFlow()).build();
    }

    public static WorkFlow voidCancelPnr() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(CancelPnrRetrieveRequestAdapter.class)
                .toMap(PnrRetrieveNetworkCall.class, retry(2).onError(CancelPnrWorkflowBuilder::retryable))
                .toMap(VoidCancelValidateTask.class)
                .toMap(VoidCancelRequestAdapterTask.class)
                .toMap(VoidCancelPnrNetworkCallTask.class)
                .toMap(VoidCancelPnrResponseAdapterTask.class)
                .toMap(DummyTask.class, completeFlow()).build();
    }

    private static boolean retryable(Throwable e) {
        return e instanceof PSErrorException && ((PSErrorException) e).getPsErrorEnum() == com.mmt.flights.postsales.error.PSCommonErrorEnum.SUPPLIER_ERROR;
    }
}
