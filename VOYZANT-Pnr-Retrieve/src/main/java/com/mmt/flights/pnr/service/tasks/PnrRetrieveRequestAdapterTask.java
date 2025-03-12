package com.mmt.flights.pnr.service.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import org.springframework.stereotype.Component;

@Component
public class PnrRetrieveRequestAdapterTask implements MapTask {

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        String supplierPnrRequest = null;
        return flowState.toBuilder().addValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_REQUEST, supplierPnrRequest).build();
    }
}
