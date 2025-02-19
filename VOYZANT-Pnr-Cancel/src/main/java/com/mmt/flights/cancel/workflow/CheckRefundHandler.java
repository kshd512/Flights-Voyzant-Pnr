package com.mmt.flights.cancel.workflow;

import com.mmt.api.rxflow.FlowHandler;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.HiveRequestResponseLogger;
import com.mmt.flights.postsales.logger.FunnelStep;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseDTO;
import com.mmt.flights.supply.common.SupplyErrorDetailDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;

import java.util.HashMap;


public class CheckRefundHandler extends CancellationBaseHandler implements FlowHandler<SupplyPnrCancelRequestDTO, SupplyPnrCancelResponseDTO> {

    public CheckRefundHandler(HiveRequestResponseLogger hiveLogger) {
        super(hiveLogger, FunnelStep.EmdRefund);
    }

    @Override
    public HashMap<String, Object> startAdapter(SupplyPnrCancelRequestDTO request) {
        return getMap(request);
    }

    @Override
    public SupplyPnrCancelResponseDTO successStateAdapter(FlowState resultState) {
        return resultState.getValue(FlowStateKey.RESPONSE);
    }

    @Override
    public SupplyPnrCancelResponseDTO failureStateHandler(FlowState state) {
        SupplyPnrCancelResponseDTO.Builder response = SupplyPnrCancelResponseDTO.newBuilder();
        response.setCancellationStatus(SupplyStatus.FAILURE);
        SupplyErrorDetailDTO error = logAndGetError(state);
        response.addErr(error);
        return response.build();
    }
}

