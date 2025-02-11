package com.mmt.flights.cancel.workflow.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import org.springframework.stereotype.Component;

@Component
public class CheckRefundRequestAdapterTask implements MapTask {

    @Override
    public FlowState run(FlowState flowState) throws Exception {

        return flowState.toBuilder().addValue(FlowStateKey.CHECK_REFUND_REQUEST, null).build();
    }
}
