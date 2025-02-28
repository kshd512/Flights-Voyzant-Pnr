package com.mmt.flights.odc.service;

import com.mmt.api.rxflow.WorkFlow;
import com.mmt.flights.odc.service.tasks.ODCSearchInvokerTask;
import com.mmt.flights.odc.service.tasks.ODCSearchRequestBuilderTask;
import com.mmt.flights.odc.service.tasks.ODCSearchResponseAdapterTask;
import com.mmt.flights.pnr.workflow.tasks.CMSManagerTask;
import com.mmt.flights.pnr.workflow.tasks.PnrRetrieveNetworkCall;
import com.mmt.flights.pnr.workflow.tasks.PnrRetrieveRequestAdapter;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.stereotype.Component;

import static com.mmt.api.rxflow.rule.Rules.completeFlow;
import static com.mmt.api.rxflow.rule.Rules.retry;

@Component
public class ODCWorkflowBuilder {

    public static WorkFlow odcSearchV1() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(PnrRetrieveRequestAdapter.class)
                .toMap(PnrRetrieveNetworkCall.class, retry(2).onError(ODCWorkflowBuilder::retryable))
                .toMap(ODCSearchRequestBuilderTask.class)
                .toMap(ODCSearchInvokerTask.class, retry(2).onError(ODCWorkflowBuilder::retryable))
                .toMap(ODCSearchResponseAdapterTask.class, completeFlow())
                .build();
    }

    private static boolean retryable(Throwable e) {
        return e instanceof PSErrorException && 
               ((PSErrorException) e).getPsErrorEnum() == PSCommonErrorEnum.SUPPLIER_ERROR;
    }
}
