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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CancelPnrRequestAdapterTask implements MapTask {

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private CommonDocumentService commonDocumentService;

    @Override
    public FlowState run(FlowState flowState) throws Exception {

        String supplierPNRResponse = flowState.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        OrderViewRS retrieveResponse = objectMapper.readValue(supplierPNRResponse, OrderViewRS.class);
        CancelPnrRequest cancelPnrRequest = createCancelPnrRequest(retrieveResponse);
        String cancelPnrRequestJson = objectMapper.writeValueAsString(cancelPnrRequest);

        return flowState.toBuilder()
                .addValue(FlowStateKey.CANCEL_PNR_REQUEST, cancelPnrRequestJson)
                .build();
    }

    private CancelPnrRequest createCancelPnrRequest(OrderViewRS retrieveResponse) {
        CancelPnrRequest cancelPnrRequest = new CancelPnrRequest();
        OrderCancelRQ orderCancelRQ = new OrderCancelRQ();

        orderCancelRQ.setDocument(commonDocumentService.createDocument());
        orderCancelRQ.setParty(commonDocumentService.createParty());

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
