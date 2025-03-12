package com.mmt.flights.cancel.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.service.CommonDocumentService;
import com.mmt.flights.entity.cancel.request.OrderCancelRQ;
import com.mmt.flights.entity.cancel.request.Query;
import com.mmt.flights.entity.cancel.request.VoidPnrRequest;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoidCancelRequestAdapterTask implements MapTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoidCancelRequestAdapterTask.class);

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private CommonDocumentService commonDocumentService;

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        LOGGER.info("Starting VoidCancelRequestAdapterTask");
        
        String supplierPNRResponse = flowState.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        SupplyPnrCancelRequestDTO supplyPnrRequestDTO = flowState.getValue(FlowStateKey.REQUEST);
        
        // Convert supplier PNR response to OrderViewRS
        OrderViewRS retrieveResponse = objectMapper.readValue(supplierPNRResponse, OrderViewRS.class);

        // Create and populate the Void PNR Request
        VoidPnrRequest voidPnrRequest = createVoidPnrRequest(retrieveResponse, supplyPnrRequestDTO);

        // Convert to JSON
        String voidPnrRequestJson = objectMapper.writeValueAsString(voidPnrRequest);

        LOGGER.info("VoidCancelRequestAdapterTask completed successfully");
        
        // Add to FlowState and return
        return flowState.toBuilder()
                .addValue(FlowStateKey.VOID_PNR_REQUEST, voidPnrRequestJson)
                .build();
    }

    private VoidPnrRequest createVoidPnrRequest(OrderViewRS retrieveResponse, SupplyPnrCancelRequestDTO supplyPnrRequestDTO) {
        VoidPnrRequest voidPnrRequest = new VoidPnrRequest();
        OrderCancelRQ orderCancelRQ = new OrderCancelRQ();

        orderCancelRQ.setDocument(commonDocumentService.createDocument());
        orderCancelRQ.setParty(commonDocumentService.createParty());
        orderCancelRQ.setQuery(createQuery(retrieveResponse));

        voidPnrRequest.setAirTicketVoidRQ(orderCancelRQ);
        return voidPnrRequest;
    }

    private Query createQuery(OrderViewRS retrieveResponse) {
        Query query = new Query();
        setOrderDetails(query, retrieveResponse);
        setTicketNumbers(query, retrieveResponse);
        query.setNeedToCancelBooking("Y");
        return query;
    }

    private void setOrderDetails(Query query, OrderViewRS retrieveResponse) {
        if (retrieveResponse.getOrder() != null && !retrieveResponse.getOrder().isEmpty()) {
            query.setOrderID(retrieveResponse.getOrder().get(0).getOrderID());
            query.setGdsBookingReference(new String[]{retrieveResponse.getOrder().get(0).getGdsBookingReference()});
        }
    }

    private void setTicketNumbers(Query query, OrderViewRS retrieveResponse) {
        if (retrieveResponse.getTicketDocInfos() != null && 
            retrieveResponse.getTicketDocInfos().getTicketDocInfo() != null &&
            !retrieveResponse.getTicketDocInfos().getTicketDocInfo().isEmpty()) {
            
            String[] ticketNumbers = retrieveResponse.getTicketDocInfos().getTicketDocInfo().stream()
                .map(ticketDocInfo -> ticketDocInfo.getTicketDocument().getTicketDocNbr())
                .toArray(String[]::new);
            
            query.setTicketNumber(ticketNumbers);
        }
    }
}