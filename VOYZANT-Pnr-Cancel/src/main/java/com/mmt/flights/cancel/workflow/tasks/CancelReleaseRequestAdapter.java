package com.mmt.flights.cancel.workflow.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.entity.cms.CMSMapHolder;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class CancelReleaseRequestAdapter implements MapTask {

    @Override
    public FlowState run(FlowState flowState) throws Exception {

        SupplyPnrCancelRequestDTO request = flowState.getValue(FlowStateKey.REQUEST);
        CMSMapHolder cmsMap = flowState.getValue(FlowStateKey.CMS_MAP);

        String cancelReleaseRequest = null;

        return flowState.toBuilder().addValue(FlowStateKey.CANCEL_RELEASE_REQUEST, cancelReleaseRequest).build();

    }
}
