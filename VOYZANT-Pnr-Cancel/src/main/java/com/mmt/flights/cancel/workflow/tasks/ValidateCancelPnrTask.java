package com.mmt.flights.cancel.workflow.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyValidateCancelResponseDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import com.mmt.flights.supply.pnr.v4.request.SupplyPaxInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ValidateCancelPnrTask implements MapTask {

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        SupplyPnrCancelRequestDTO request = flowState.getValue(FlowStateKey.REQUEST);
        String supplierPNRResponse = flowState.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);

        validateRequestFromResponse(supplierPNRResponse, request);

        SupplyValidateCancelResponseDTO.Builder validateCancelResponse = SupplyValidateCancelResponseDTO.newBuilder();
        validateCancelResponse.setStatus(SupplyStatus.SUCCESS);
        return flowState.toBuilder().addValue(FlowStateKey.RESPONSE, validateCancelResponse.build())
                .build();
    }

    private void validateRequestFromResponse(String supplierPNRResponse, SupplyPnrCancelRequestDTO request) {
        validateBasicRequest();
        if (!request.getRequestCore().getPaxInfoList().isEmpty()) {
            partialPaxCancellationValidation();
        }
    }

    private void validatePassengerExistence() {
    }

    private void partialPaxCancellationValidation() {
        canCancelInPartialPaxCancellation();
    }

    private void validateBasicRequest() {
        // Check if the PNR is already cancelled or not issued


        // Check if the departure date is in the past


        // Check if all passengers in the request exist in the PNR
        validatePassengerExistence();
    }

    public void canCancelInPartialPaxCancellation() {

    }

}
