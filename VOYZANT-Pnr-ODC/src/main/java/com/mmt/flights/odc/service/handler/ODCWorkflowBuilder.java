package com.mmt.flights.odc.service.handler;

import com.mmt.api.rxflow.WorkFlow;
import com.mmt.flights.odc.service.tasks.*;
import com.mmt.flights.pnr.service.tasks.CMSManagerTask;
import com.mmt.flights.pnr.service.tasks.PnrRetrieveNetworkCall;
import com.mmt.flights.pnr.service.tasks.PnrRetrieveRequestAdapter;
import org.springframework.stereotype.Component;

import static com.mmt.api.rxflow.rule.Rules.completeFlow;

@Component
public class ODCWorkflowBuilder {

    public static WorkFlow odcSearch() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(PnrRetrieveRequestAdapter.class)
                .toMap(PnrRetrieveNetworkCall.class)
                .toMap(ODCSearchRequestBuilderTask.class)
                .toMap(ODCSearchInvokerTask.class)
                .toMap(ODCSearchResponseAdapterTask.class, completeFlow())
                .build();
    }

    public static WorkFlow odcPrePayment() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(PnrRetrieveRequestAdapter.class)
                .toMap(PnrRetrieveNetworkCall.class)
                .toMap(ODCExchangePriceRequestBuilderTask.class)
                .toMap(ODCExchangePriceInvokerTask.class)
                .toMap(ODCExchangePriceResponseAdapterTask.class, completeFlow())
                .build();
    }

    public static WorkFlow odcCommit() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(PnrRetrieveRequestAdapter.class)
                .toMap(PnrRetrieveNetworkCall.class)
                .toMap(ODCBookRequestBuilderTask.class)
                .toMap(ODCBookInvokerTask.class)
                .toMap(ODCBookResponseAdapterTask.class, completeFlow())
                .build();
    }
}
