package com.mmt.flights.cancel.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.service.CommonDocumentService;
import com.mmt.flights.entity.common.Query;
import com.mmt.flights.entity.pnr.retrieve.request.OrderRetreiveRQ;
import com.mmt.flights.entity.pnr.retrieve.request.OrderRetrieveRequest;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CancelPnrRetrieveRequestAdapterTask implements MapTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CancelPnrRetrieveRequestAdapterTask.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommonDocumentService commonDocumentService;

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        LOGGER.info("Starting CancelPnrRetrieveRequestAdapterTask");
        
        SupplyPnrCancelRequestDTO supplyPnrRequestDTO = flowState.getValue(FlowStateKey.REQUEST);
        
        // Create Order Retrieve Request
        OrderRetrieveRequest orderRetrieveRequest = createOrderRetrieveRequest(supplyPnrRequestDTO);
        
        // Convert to JSON
        String retrievePnrRequest = objectMapper.writeValueAsString(orderRetrieveRequest);
        
        LOGGER.info("CancelPnrRetrieveRequestAdapterTask completed successfully");
        
        return flowState.toBuilder()
                .addValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_REQUEST, retrievePnrRequest)
                .build();
    }

    /**
     * Creates an OrderRetrieveRequest object
     */
    private OrderRetrieveRequest createOrderRetrieveRequest(SupplyPnrCancelRequestDTO supplyPnrRequestDTO) {
        OrderRetrieveRequest orderRetrieveRequest = new OrderRetrieveRequest();
        OrderRetreiveRQ orderRetreiveRQ = createOrderRetrieveRQ(supplyPnrRequestDTO);
        orderRetrieveRequest.setOrderRetreiveRQ(orderRetreiveRQ);
        return orderRetrieveRequest;
    }

    /**
     * Creates an OrderRetreiveRQ object with document, party, and query
     */
    private OrderRetreiveRQ createOrderRetrieveRQ(SupplyPnrCancelRequestDTO supplyPnrRequestDTO) {
        OrderRetreiveRQ orderRetreiveRQ = new OrderRetreiveRQ();
        
        // Use CommonDocumentService for document and party
        orderRetreiveRQ.setDocument(commonDocumentService.createDocument());
        orderRetreiveRQ.setParty(commonDocumentService.createParty());
        
        // Set query with PNR details
        orderRetreiveRQ.setQuery(createQuery(supplyPnrRequestDTO));
        
        return orderRetreiveRQ;
    }

    /**
     * Creates a Query object with the PNR information
     */
    private Query createQuery(SupplyPnrCancelRequestDTO supplyPnrRequestDTO) {
        Query query = new Query();
        String pnr = supplyPnrRequestDTO.getRequestCore().getSupplierPnr();
        query.setOrderId(pnr);
        query.setGdsBookingReference(new String[]{pnr});
        return query;
    }
}
