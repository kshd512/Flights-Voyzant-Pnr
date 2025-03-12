package com.mmt.flights.pnr.service.handler;

import com.mmt.api.rxflow.WorkFlow;
import com.mmt.flights.pnr.service.tasks.*;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.stereotype.Component;

import static com.mmt.api.rxflow.rule.Rules.completeFlow;
import static com.mmt.api.rxflow.rule.Rules.retry;

@Component
public class PnrWorkFlowBuilder {

    public static WorkFlow retrievePnr() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(PnrRetrieveRequestAdapterTask.class)
                .toMap(PnrRetrieveInvokerTask.class, retry(2).onError(PnrWorkFlowBuilder::retryable))
                .toMap(PnrRetrieveResponseAdapterTask.class)
                .toMap(DummyTask.class, completeFlow()).build();
    }

    private static boolean retryable(Throwable e) {
        return e instanceof PSErrorException && ((PSErrorException) e).getPsErrorEnum() == com.mmt.flights.postsales.error.PSCommonErrorEnum.SUPPLIER_ERROR;
    }
}

