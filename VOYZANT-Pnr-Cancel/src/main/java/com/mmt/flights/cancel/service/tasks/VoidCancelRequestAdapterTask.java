package com.mmt.flights.cancel.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.service.CommonDocumentService;
import com.mmt.flights.entity.cancel.request.OrderCancelRQ;
import com.mmt.flights.entity.cancel.request.VoidPnrRequest;
import com.mmt.flights.entity.common.Query;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoidCancelRequestAdapterTask implements MapTask {

    //@Autowired
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private CommonDocumentService commonDocumentService;

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        String supplierPNRResponse = flowState.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        OrderViewRS retrieveResponse = objectMapper.readValue(supplierPNRResponse, OrderViewRS.class);
        VoidPnrRequest voidPnrRequest = createVoidPnrRequest(retrieveResponse);
        String voidPnrRequestJson = objectMapper.writeValueAsString(voidPnrRequest);
        return flowState.toBuilder()
                .addValue(FlowStateKey.VOID_PNR_REQUEST, voidPnrRequestJson)
                .build();
    }

    private VoidPnrRequest createVoidPnrRequest(OrderViewRS retrieveResponse) {
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
            query.setOrderId(retrieveResponse.getOrder().get(0).getOrderID());
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