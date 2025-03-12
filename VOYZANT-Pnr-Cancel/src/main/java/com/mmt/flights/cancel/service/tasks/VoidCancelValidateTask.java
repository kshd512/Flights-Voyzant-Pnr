package com.mmt.flights.cancel.service.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import org.springframework.stereotype.Component;

@Component
public class VoidCancelValidateTask implements MapTask {

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        return null;
    }
}
