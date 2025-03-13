package com.mmt.flights.cancel.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.entity.cancel.response.TicketDetail;
import com.mmt.flights.entity.cancel.response.VoidPnrResponse;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseMetaDataDTO;
import com.mmt.flights.supply.common.SupplyErrorDetailDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VoidCancelPnrResponseAdapterTask implements MapTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoidCancelPnrResponseAdapterTask.class);

    //@Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FlowState run(FlowState state) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            String voidPnrResponseStr = state.getValue(FlowStateKey.VOID_PNR_RESPONSE);
            VoidPnrResponse voidPnrResponse = objectMapper.readValue(voidPnrResponseStr, VoidPnrResponse.class);
            SupplyPnrCancelResponseDTO responseDTO = adaptToSupplyPnrCancelResponseDTO(voidPnrResponse, state);
            state = state.toBuilder().addValue(FlowStateKey.SUPPLY_PNR_CANCEL_RESPONSE, responseDTO).build();
        } catch (Exception e) {
            LOGGER.error("Error in VoidCancelPnrResponseAdapterTask", e);
            throw new PSErrorException("Error adapting void PNR response", PSCommonErrorEnum.FLT_UNKNOWN_ERROR);
        } finally {
            MMTLogger.logTime(state, MetricServices.VOID_CANCEL_PNR_RESPONSE_ADAPTER_TASK_LATENCY.name(), startTime);
        }
        return state;
    }
    
    private SupplyPnrCancelResponseDTO adaptToSupplyPnrCancelResponseDTO(VoidPnrResponse voidPnrResponse, FlowState state) {
        SupplyPnrCancelResponseDTO.Builder builder = SupplyPnrCancelResponseDTO.newBuilder();
        builder.setMeta(buildMetadata(state));
        boolean isSuccessful = isVoidOperationSuccessful(voidPnrResponse);
        builder.setCancellationStatus(SupplyStatus.FAILURE);
        if(isSuccessful){
            builder.setCancellationStatus(SupplyStatus.SUCCESS);
        }
        builder.setRefundStatus(SupplyStatus.ST_NOT_SET);
        if (!isSuccessful) {
            List<SupplyErrorDetailDTO> errors = extractErrors(voidPnrResponse);
            builder.addAllErr(errors);
        }
        Map<String, String> supplierInfo = extractSupplierCancellationInfo(voidPnrResponse);
        builder.putAllSupplierCancellationInfo(supplierInfo);
        return builder.build();
    }
    
    private SupplyPnrCancelResponseMetaDataDTO buildMetadata(FlowState state) {
        SupplyPnrCancelResponseMetaDataDTO.Builder metaBuilder = SupplyPnrCancelResponseMetaDataDTO.newBuilder();
        metaBuilder.setServiceName("VOID_PNR_SERVICE");
        metaBuilder.setLob("FLIGHTS");
        Map<String, String> traceInfo = new HashMap<>();
        traceInfo.put("operation", "VOID_TICKET");
        metaBuilder.putAllTraceInfo(traceInfo);
        
        return metaBuilder.build();
    }
    
    private boolean isVoidOperationSuccessful(VoidPnrResponse response) {
        if (response == null || response.getTicketVoidRS() == null || 
            response.getTicketVoidRS().getResult() == null) {
            return false;
        }
        
        String status = response.getTicketVoidRS().getResult().getStatus();
        return "SUCCESS".equalsIgnoreCase(status);
    }
    
    private List<SupplyErrorDetailDTO> extractErrors(VoidPnrResponse response) {
        List<SupplyErrorDetailDTO> errors = new ArrayList<>();
        
        if (response != null && response.getTicketVoidRS() != null && 
            response.getTicketVoidRS().getResult() != null) {
            
            // Add general error message if present
            String errorMessage = response.getTicketVoidRS().getResult().getErrorMessage();
            if (errorMessage != null && !errorMessage.isEmpty()) {
                SupplyErrorDetailDTO.Builder errorBuilder = SupplyErrorDetailDTO.newBuilder();
                errorBuilder.setEc("VOID_FAILED");
                errorBuilder.setEm(errorMessage);
                errorBuilder.setStatusCode("ERROR");
                errors.add(errorBuilder.build());
            }

            if (response.getTicketVoidRS().getResult().getTicketDetails() != null) {
                for (TicketDetail detail : response.getTicketVoidRS().getResult().getTicketDetails()) {
                    if (!"SUCCESS".equalsIgnoreCase(detail.getStatus())) {
                        SupplyErrorDetailDTO.Builder ticketErrorBuilder = SupplyErrorDetailDTO.newBuilder();
                        ticketErrorBuilder.setEc("TICKET_VOID_FAILED");
                        ticketErrorBuilder.setEm("Failed to void ticket: " + detail.getDocumentNumber());
                        ticketErrorBuilder.setEd(detail.getMsg());
                        ticketErrorBuilder.setStatusCode("ERROR");
                        errors.add(ticketErrorBuilder.build());
                    }
                }
            }
        }

        if (errors.isEmpty()) {
            SupplyErrorDetailDTO.Builder defaultErrorBuilder = SupplyErrorDetailDTO.newBuilder();
            defaultErrorBuilder.setEc("VOID_OPERATION_FAILED");
            defaultErrorBuilder.setEm("Failed to void tickets");
            defaultErrorBuilder.setStatusCode("ERROR");
            errors.add(defaultErrorBuilder.build());
        }
        return errors;
    }
    
    private Map<String, String> extractSupplierCancellationInfo(VoidPnrResponse response) {
        Map<String, String> info = new HashMap<>();
        
        if (response != null && response.getTicketVoidRS() != null && 
            response.getTicketVoidRS().getResult() != null) {
            String bookingStatus = response.getTicketVoidRS().getResult().getBookingStatus();
            if (bookingStatus != null) {
                info.put("bookingStatus", bookingStatus);
            }
            String requestId = response.getTicketVoidRS().getResult().getTktRequestId();
            if (requestId != null) {
                info.put("tktRequestId", requestId);
            }
            String shoppingResponseId = response.getTicketVoidRS().getResult().getShoppingResponseId();
            if (shoppingResponseId != null) {
                info.put("shoppingResponseId", shoppingResponseId);
            }

            if (response.getTicketVoidRS().getResult().getTicketDetails() != null) {
                StringBuilder ticketNumbers = new StringBuilder();
                for (TicketDetail detail : response.getTicketVoidRS().getResult().getTicketDetails()) {
                    if (detail.getDocumentNumber() != null) {
                        if (ticketNumbers.length() > 0) {
                            ticketNumbers.append(",");
                        }
                        ticketNumbers.append(detail.getDocumentNumber());
                    }
                }
                if (ticketNumbers.length() > 0) {
                    info.put("ticketNumbers", ticketNumbers.toString());
                }
            }
        }
        return info;
    }
}