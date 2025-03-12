package com.mmt.flights.cancel.service.handler;

import com.mmt.api.rxflow.FlowHandler;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.HiveRequestResponseLogger;
import com.mmt.flights.postsales.logger.FunnelStep;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyValidateCancelResponseDTO;
import com.mmt.flights.supply.common.SupplyErrorDetailDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;

import java.util.HashMap;

public class CancellationValidationHandler extends CancellationBaseHandler implements FlowHandler<SupplyPnrCancelRequestDTO, SupplyValidateCancelResponseDTO> {

    public CancellationValidationHandler(HiveRequestResponseLogger hiveLogger) {
        super(hiveLogger, FunnelStep.Validate);
    }

    @Override
    public HashMap<String, Object> startAdapter(SupplyPnrCancelRequestDTO request) {
        return getMap(request);
    }

    @Override
    public SupplyValidateCancelResponseDTO successStateAdapter(FlowState resultState) {
        return resultState.getValue(FlowStateKey.RESPONSE);
    }

    @Override
    public SupplyValidateCancelResponseDTO failureStateHandler(FlowState state) {
        SupplyValidateCancelResponseDTO.Builder response = SupplyValidateCancelResponseDTO.newBuilder();
        response.setStatus(SupplyStatus.FAILURE);
        SupplyErrorDetailDTO error = logAndGetError(state);
        response.addErr(error);
        return response.build();
    }
}
