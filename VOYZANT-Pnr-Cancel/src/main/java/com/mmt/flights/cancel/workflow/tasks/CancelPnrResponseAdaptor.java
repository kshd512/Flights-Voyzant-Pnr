package com.mmt.flights.cancel.workflow.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import org.springframework.stereotype.Component;

import static com.mmt.flights.common.constants.CommonConstants.EMPTY_STRING;

@Component
public class CancelPnrResponseAdaptor implements MapTask {
    @Override
    public FlowState run(FlowState state) throws Exception {

        SupplyPnrCancelRequestDTO supplyPnrRequestDTO = state.getValue(FlowStateKey.REQUEST);
        String cancelResponse = state.getValue(FlowStateKey.CANCEL_PNR_RESPONSE);
        SupplyPnrCancelResponseDTO.Builder response = SupplyPnrCancelResponseDTO.newBuilder();
        if (response.getCancellationStatus() != null ) {
            response.setCancellationStatus(SupplyStatus.SUCCESS);
            response.setRefundStatus(SupplyStatus.SUCCESS);
                response.setSplitPnr(state
                        .getValue(FlowStateKey.GetPnr));
        } else {
            response.setCancellationStatus(SupplyStatus.FAILURE);
            response.setRefundStatus(SupplyStatus.FAILURE);
        }
        

		response.setPenalty(EMPTY_STRING);

        return state.toBuilder()
                .addValue(FlowStateKey.PNR_CANCEL_RESPONSE,response.build())
                .build();
    }
}
