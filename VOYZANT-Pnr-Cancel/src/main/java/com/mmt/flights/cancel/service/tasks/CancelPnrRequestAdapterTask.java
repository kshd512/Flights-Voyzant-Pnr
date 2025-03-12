package com.mmt.flights.cancel.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.service.CommonDocumentService;
import com.mmt.flights.entity.cancel.request.CancelPnrRequest;
import com.mmt.flights.entity.cancel.request.OrderCancelRQ;
import com.mmt.flights.entity.common.Query;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CancelPnrRequestAdapterTask implements MapTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CancelPnrRequestAdapterTask.class);

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private CommonDocumentService commonDocumentService;

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        LOGGER.info("Starting CancelPnrRequestAdapterTask");
        
        String supplierPNRResponse = flowState.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);

        // Convert supplier PNR response to OrderViewRS
        OrderViewRS retrieveResponse = objectMapper.readValue(supplierPNRResponse, OrderViewRS.class);

        // Create Cancel PNR Request
        CancelPnrRequest cancelPnrRequest = createCancelPnrRequest(retrieveResponse);

        // Convert to JSON
        String cancelPnrRequestJson = objectMapper.writeValueAsString(cancelPnrRequest);

        LOGGER.info("CancelPnrRequestAdapterTask completed successfully");
        
        return flowState.toBuilder()
                .addValue(FlowStateKey.CANCEL_PNR_REQUEST, cancelPnrRequestJson)
                .build();
    }

    private CancelPnrRequest createCancelPnrRequest(OrderViewRS retrieveResponse) {
        CancelPnrRequest cancelPnrRequest = new CancelPnrRequest();
        OrderCancelRQ orderCancelRQ = new OrderCancelRQ();
        
        // Set Document and Party using CommonDocumentService
        orderCancelRQ.setDocument(commonDocumentService.createDocument());
        orderCancelRQ.setParty(commonDocumentService.createParty());
        
        // Set Query
        orderCancelRQ.setQuery(createQuery(retrieveResponse));
        
        cancelPnrRequest.setOrderCancelRQ(orderCancelRQ);
        return cancelPnrRequest;
    }
    
    private Query createQuery(OrderViewRS retrieveResponse) {
        Query query = new Query();
        setOrderDetails(query, retrieveResponse);
        return query;
    }
    
    private void setOrderDetails(Query query, OrderViewRS retrieveResponse) {
        if (retrieveResponse.getOrder() != null && !retrieveResponse.getOrder().isEmpty()) {
            query.setOrderId(retrieveResponse.getOrder().get(0).getOrderID());
            query.setGdsBookingReference(new String[]{retrieveResponse.getOrder().get(0).getGdsBookingReference()});
        }
    }
}
