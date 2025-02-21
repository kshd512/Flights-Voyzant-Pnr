package com.mmt.flights.cancel.workflow.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.entity.pnr.retrieve.request.*;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

@Component
public class CancelPnrRetrieveRequestAdapter implements MapTask {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        SupplyPnrCancelRequestDTO supplyPnrRequestDTO = flowState.getValue(FlowStateKey.REQUEST);
        String splitPnrResponse = flowState.getValue(FlowStateKey.SPLIT_PNR_RESPONSE);
        
        OrderRetrieveRequest orderRetrieveRequest = new OrderRetrieveRequest();
        OrderRetreiveRQ orderRetreiveRQ = new OrderRetreiveRQ();
        
        // Set Document
        Document document = new Document();
        document.setName("MMT");
        document.setReferenceversion("1.0");
        orderRetreiveRQ.setDocument(document);
        
        // Set Party
        Party party = new Party();
        Sender sender = new Sender();
        TravelAgencySender travelAgencySender = new TravelAgencySender();
        travelAgencySender.setName("MMT");
        travelAgencySender.setIataNumber("");
        travelAgencySender.setAgencyId("");
        
        // Set Contacts
        Contacts contacts = new Contacts();
        Contact contact = new Contact();
        contact.setEmailContact("pst@claritytts.com");
        contacts.setContact(Arrays.asList(contact));
        travelAgencySender.setContacts(contacts);
        
        sender.setTravelAgencySender(travelAgencySender);
        party.setSender(sender);
        orderRetreiveRQ.setParty(party);
        
        // Set Query
        Query query = new Query();
        
        // If splitPnrResponse exists, use the split PNR for retrieval
        if (splitPnrResponse != null) {
            // Convert splitPnrResponse to its schema bean
            com.mmt.flights.entity.split.response.AirSplitPnrResponse splitPnrResponseObj = 
                objectMapper.readValue(splitPnrResponse, com.mmt.flights.entity.split.response.AirSplitPnrResponse.class);
            
            // Use the split PNR details
            if (splitPnrResponseObj != null && splitPnrResponseObj.getAirSplitPnrRS() != null) {
                String orderId = splitPnrResponseObj.getAirSplitPnrRS().getSplitedOrderID();
                String gdsRef = splitPnrResponseObj.getAirSplitPnrRS().getSplitedGdsBookingReference();
                query.setOrderId(orderId);
                query.setGdsBookingReference(Collections.singletonList(gdsRef));
            }
        } else {
            // Use original PNR details from the request
            String pnr = supplyPnrRequestDTO.getRequestCore().getSupplierPnr();
            query.setOrderId(pnr);
            query.setGdsBookingReference(Collections.singletonList(pnr));
        }
        
        orderRetreiveRQ.setQuery(query);
        orderRetrieveRequest.setOrderRetreiveRQ(orderRetreiveRQ);
        
        String retrievePnrRequest = objectMapper.writeValueAsString(orderRetrieveRequest);
        
        return flowState.toBuilder()
                .addValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_REQUEST, retrievePnrRequest)
                .build();
    }
}
