package com.mmt.flights.cancel.service.handler;

import com.mmt.api.rxflow.WorkFlow;
import com.mmt.flights.cancel.service.tasks.*;
import com.mmt.flights.pnr.service.tasks.CMSManagerTask;
import com.mmt.flights.pnr.service.tasks.DummyTask;
import com.mmt.flights.pnr.service.tasks.PnrRetrieveInvokerTask;
import org.springframework.stereotype.Component;

import static com.mmt.api.rxflow.rule.Rules.completeFlow;

@Component
public class CancelPnrWorkflowBuilder {

    public static WorkFlow validateCancelPnr() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(CancelPnrRetrieveRequestAdapterTask.class)
                .toMap(PnrRetrieveInvokerTask.class)
                .toMap(ValidateCancelPnrTask.class)
                .toMap(CancelPnrResponseAdaptorTask.class, completeFlow()).build();
    }

    public static WorkFlow cancelPnr() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(CancelPnrRetrieveRequestAdapterTask.class)
                .toMap(PnrRetrieveInvokerTask.class)
                .toMap(ValidateCancelPnrTask.class)
                .toMap(CancelPnrRequestAdapterTask.class)
                .toMap(CancelPnrNetworkCallTask.class)
                .toMap(CancelPnrResponseAdaptorTask.class, completeFlow()).build();
    }

    public static WorkFlow voidCancelPnr() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(CancelPnrRetrieveRequestAdapterTask.class)
                .toMap(PnrRetrieveInvokerTask.class)
                .toMap(VoidCancelValidateTask.class)
                .toMap(VoidCancelRequestAdapterTask.class)
                .toMap(VoidCancelPnrInvokerTask.class)
                .toMap(VoidCancelPnrResponseAdapterTask.class)
                .toMap(DummyTask.class, completeFlow()).build();
    }
}
