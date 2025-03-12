package com.mmt.flights.cancel.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.entity.cancel.common.*;
import com.mmt.flights.entity.cancel.request.OrderCancelRQ;
import com.mmt.flights.entity.cancel.request.Query;
import com.mmt.flights.entity.cancel.request.VoidPnrRequest;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class VoidCancelRequestAdapterTask implements MapTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoidCancelRequestAdapterTask.class);

    @Autowired
    private ObjectMapper objectMapper;

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

        // Set Document
        Document document = new Document();
        document.setName("MMT");
        document.setReferenceVersion("1.0");
        orderCancelRQ.setDocument(document);

        // Set Party
        Party party = new Party();
        Sender sender = new Sender();
        TravelAgencySender travelAgencySender = new TravelAgencySender();
        travelAgencySender.setName("MMT");
        travelAgencySender.setIataNumber("");
        travelAgencySender.setAgencyID("");

        // Set Contacts
        Contacts contacts = new Contacts();
        Contact contact = new Contact();
        contact.setEmailContact("pst@claritytts.com");
        contacts.setContact(Collections.singletonList(contact));
        travelAgencySender.setContacts(contacts);

        sender.setTravelAgencySender(travelAgencySender);
        party.setSender(sender);
        orderCancelRQ.setParty(party);

        Query query = new Query();
        if (retrieveResponse.getOrder() != null && !retrieveResponse.getOrder().isEmpty()) {
            // Use OrderID from the retrieved response
            query.setOrderID(retrieveResponse.getOrder().get(0).getOrderID());
            // Use GDS Booking Reference from the retrieved response
            query.setGdsBookingReference(new String[]{retrieveResponse.getOrder().get(0).getGdsBookingReference()});
        }

        orderCancelRQ.setQuery(query);
        voidPnrRequest.setAirTicketVoidRQ(orderCancelRQ);

        return voidPnrRequest;
    }
}