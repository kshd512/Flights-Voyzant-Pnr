package com.mmt.flights.pnr.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.service.CommonDocumentService;
import com.mmt.flights.entity.common.Query;
import com.mmt.flights.entity.pnr.retrieve.request.OrderRetreiveRQ;
import com.mmt.flights.entity.pnr.retrieve.request.OrderRetrieveRequest;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class PnrRetrieveRequestAdapterTask implements MapTask {

    //@Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CommonDocumentService commonDocumentService;

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        SupplyPnrCancelRequestDTO supplyPnrRequestDTO = flowState.getValue(FlowStateKey.REQUEST);
        OrderRetrieveRequest orderRetrieveRequest = createOrderRetrieveRequest(supplyPnrRequestDTO);
        String retrievePnrRequest = objectMapper.writeValueAsString(orderRetrieveRequest);
        return flowState.toBuilder()
                .addValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_REQUEST, retrievePnrRequest)
                .build();
    }

    private OrderRetrieveRequest createOrderRetrieveRequest(SupplyPnrCancelRequestDTO supplyPnrRequestDTO) {
        OrderRetrieveRequest orderRetrieveRequest = new OrderRetrieveRequest();
        OrderRetreiveRQ orderRetreiveRQ = createOrderRetrieveRQ(supplyPnrRequestDTO);
        orderRetrieveRequest.setOrderRetreiveRQ(orderRetreiveRQ);
        return orderRetrieveRequest;
    }

    private OrderRetreiveRQ createOrderRetrieveRQ(SupplyPnrCancelRequestDTO supplyPnrRequestDTO) {
        OrderRetreiveRQ orderRetreiveRQ = new OrderRetreiveRQ();
        orderRetreiveRQ.setDocument(commonDocumentService.createDocument());
        orderRetreiveRQ.setParty(commonDocumentService.createParty());
        orderRetreiveRQ.setQuery(createQuery(supplyPnrRequestDTO));
        
        return orderRetreiveRQ;
    }

    private Query createQuery(SupplyPnrCancelRequestDTO supplyPnrRequestDTO) {
        Query query = new Query();
        String pnr = supplyPnrRequestDTO.getRequestCore().getSupplierPnr();
        query.setOrderId(pnr);
        query.setGdsBookingReference(new String[]{pnr});
        return query;
    }
}
