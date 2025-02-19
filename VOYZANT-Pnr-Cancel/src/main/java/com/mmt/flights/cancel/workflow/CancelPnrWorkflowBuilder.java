package com.mmt.flights.cancel.workflow;

import com.mmt.api.rxflow.WorkFlow;
import com.mmt.flights.cancel.workflow.tasks.*;
import com.mmt.flights.pnr.workflow.tasks.CMSManagerTask;
import com.mmt.flights.pnr.workflow.tasks.DummyTask;
import com.mmt.flights.pnr.workflow.tasks.PnrRetrieveNetworkCall;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.split.workflow.tasks.SplitPnrNetworkCallTask;
import com.mmt.flights.split.workflow.tasks.SplitPnrRequestAdapterTask;
import com.mmt.flights.split.workflow.tasks.ValidateSplitPnrTask;
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
                .toMap(DummyTask.class, completeFlow()).build();
    }

    public static WorkFlow cancelPnr() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(CancelPnrRetrieveRequestAdapter.class)
                .toMap(PnrRetrieveNetworkCall.class, retry(2).onError(CancelPnrWorkflowBuilder::retryable))
                .toMap(ValidateCancelPnrTask.class)
                .toMap(CancelPnrRequestAdapterTask.class)
                .toMap(CancelPnrNetworkCallTask.class)
                .toMap(DummyTask.class, completeFlow()).build();
    }

    public static WorkFlow partialPaxPnrCancel() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(CancelPnrRetrieveRequestAdapter.class)
                .toMap(PnrRetrieveNetworkCall.class, retry(2).onError(CancelPnrWorkflowBuilder::retryable))
                .toMap(ValidateCancelPnrTask.class)
                .toMap(SplitPnrRequestAdapterTask.class)
                .toMap(SplitPnrNetworkCallTask.class)
                .toMap(ValidateSplitPnrTask.class)
                .toMap(CancelPnrRequestAdapterTask.class)
                .toMap(CancelPnrNetworkCallTask.class)
                .toMap(DummyTask.class, completeFlow()).build();
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
