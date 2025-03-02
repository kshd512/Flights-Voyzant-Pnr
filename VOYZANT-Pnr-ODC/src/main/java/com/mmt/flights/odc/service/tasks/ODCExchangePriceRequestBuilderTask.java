package com.mmt.flights.odc.service.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import org.springframework.stereotype.Component;

@Component
public class ODCExchangePriceRequestBuilderTask implements MapTask {

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        return null;
    }
}
