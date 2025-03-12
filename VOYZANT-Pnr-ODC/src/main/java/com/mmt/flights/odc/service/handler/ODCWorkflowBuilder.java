package com.mmt.flights.odc.service.handler;

import com.mmt.api.rxflow.WorkFlow;
import com.mmt.flights.odc.service.tasks.*;
import com.mmt.flights.pnr.service.tasks.CMSManagerTask;
import com.mmt.flights.pnr.service.tasks.PnrRetrieveInvokerTask;
import com.mmt.flights.pnr.service.tasks.PnrRetrieveRequestAdapterTask;
import org.springframework.stereotype.Component;

import static com.mmt.api.rxflow.rule.Rules.completeFlow;

@Component
public class ODCWorkflowBuilder {

    public static WorkFlow odcSearch() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(PnrRetrieveRequestAdapterTask.class)
                .toMap(PnrRetrieveInvokerTask.class)
                .toMap(ODCSearchRequestBuilderTask.class)
                .toMap(ODCSearchInvokerTask.class)
                .toMap(ODCSearchResponseAdapterTask.class, completeFlow())
                .build();
    }

    public static WorkFlow odcPrePayment() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(PnrRetrieveRequestAdapterTask.class)
                .toMap(PnrRetrieveInvokerTask.class)
                .toMap(ODCExchangePriceRequestBuilderTask.class)
                .toMap(ODCExchangePriceInvokerTask.class)
                .toMap(ODCExchangePriceResponseAdapterTask.class, completeFlow())
                .build();
    }

    public static WorkFlow odcCommit() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(PnrRetrieveRequestAdapterTask.class)
                .toMap(PnrRetrieveInvokerTask.class)
                .toMap(ODCBookRequestBuilderTask.class)
                .toMap(ODCBookInvokerTask.class)
                .toMap(ODCBookResponseAdapterTask.class, completeFlow())
                .build();
    }
}
