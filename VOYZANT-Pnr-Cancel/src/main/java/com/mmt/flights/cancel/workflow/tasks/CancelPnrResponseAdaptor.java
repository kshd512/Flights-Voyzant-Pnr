package com.mmt.flights.cancel.workflow.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.entity.cancel.response.CancelPnrResponse;
import com.mmt.flights.entity.cancel.response.OrderViewRS;
import com.mmt.flights.entity.cancel.response.Response;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseMetaDataDTO;
import com.mmt.flights.supply.common.SupplyErrorDetailDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import org.springframework.stereotype.Component;

import static com.mmt.flights.common.constants.CommonConstants.SERVICE_NAME;
import static com.mmt.flights.common.constants.CommonConstants.SUPPLIER_NAME;

@Component
public class CancelPnrResponseAdaptor implements MapTask {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FlowState run(FlowState state) throws Exception {
        String cancelResponse = state.getValue(FlowStateKey.CANCEL_PNR_RESPONSE);

        // Parse cancel response
        CancelPnrResponse cancelPnrResponse = objectMapper.readValue(cancelResponse, CancelPnrResponse.class);
        
        // Build response
        SupplyPnrCancelResponseDTO.Builder response = SupplyPnrCancelResponseDTO.newBuilder();
        
        // Set statuses based on cancel response
        OrderViewRS orderViewRS = cancelPnrResponse.getOrderViewRS();
        if (orderViewRS != null && orderViewRS.getResponse() != null && !orderViewRS.getResponse().isEmpty()) {
            Response cancelResponseDetails = orderViewRS.getResponse().get(0);
            
            if ("SUCCESS".equalsIgnoreCase(cancelResponseDetails.getStatus()) &&
                "CANCELLED".equalsIgnoreCase(cancelResponseDetails.getBookingStatus())) {
                response.setCancellationStatus(SupplyStatus.SUCCESS);
                response.setRefundStatus(SupplyStatus.SUCCESS);
            } else {
                response.setCancellationStatus(SupplyStatus.FAILURE);
                response.setRefundStatus(SupplyStatus.FAILURE);
                
                // Add error details
                SupplyErrorDetailDTO.Builder errorBuilder = SupplyErrorDetailDTO.newBuilder();
                errorBuilder.setEc("CANCEL_FAILED");
                errorBuilder.setEm(cancelResponseDetails.getMsg());
                errorBuilder.setEd(cancelResponseDetails.getMsg());
                errorBuilder.setStatusCode("500");
                errorBuilder.setSn(SERVICE_NAME);
                response.addErr(errorBuilder.build());
            }
        } else {
            response.setCancellationStatus(SupplyStatus.FAILURE); 
            response.setRefundStatus(SupplyStatus.FAILURE);
            
            // Add error for invalid response
            SupplyErrorDetailDTO.Builder errorBuilder = SupplyErrorDetailDTO.newBuilder();
            errorBuilder.setEc("INVALID_RESPONSE");
            errorBuilder.setEm("Invalid or empty cancel response received");
            errorBuilder.setEd("Invalid or empty cancel response received");
            errorBuilder.setStatusCode("500");
            errorBuilder.setSn(SERVICE_NAME);
            response.addErr(errorBuilder.build());
        }

        // Set metadata
        SupplyPnrCancelResponseMetaDataDTO.Builder metaBuilder = SupplyPnrCancelResponseMetaDataDTO.newBuilder();
        metaBuilder.setServiceName(SERVICE_NAME);
        metaBuilder.setSupplierName(SUPPLIER_NAME);
        response.setMeta(metaBuilder.build());

        return state.toBuilder()
                .addValue(FlowStateKey.RESPONSE, response.build())
                .build();
    }
}
