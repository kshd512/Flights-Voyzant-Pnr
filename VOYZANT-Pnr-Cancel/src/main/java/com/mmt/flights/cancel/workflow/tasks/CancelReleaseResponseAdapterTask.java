package com.mmt.flights.cancel.workflow.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import org.springframework.stereotype.Component;

@Component
public class CancelReleaseResponseAdapterTask implements MapTask {

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        SupplyPnrCancelResponseDTO.Builder builder = SupplyPnrCancelResponseDTO.newBuilder();
        builder.setCancellationStatus(SupplyStatus.SUCCESS);
        builder.setRefundStatus(SupplyStatus.SUCCESS);
        return flowState.toBuilder().addValue(FlowStateKey.RESPONSE, builder.build()).build();
    }
}
