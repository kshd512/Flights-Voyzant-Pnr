package com.mmt.flights.pnr.workflow.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.CMSConstants;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.entity.cms.CMSMapHolder;
import com.mmt.flights.supply.pnr.v4.request.SupplyPnrRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class PnrRetrieveRequestAdapter implements MapTask {

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        String supplierPnrRequest = null;
        return flowState.toBuilder().addValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_REQUEST, supplierPnrRequest).build();
    }
}
