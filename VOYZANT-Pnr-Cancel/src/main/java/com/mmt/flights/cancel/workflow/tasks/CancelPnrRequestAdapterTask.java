package com.mmt.flights.cancel.workflow.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.entity.cancel.common.*;
import com.mmt.flights.entity.cancel.request.CancelPnrRequest;
import com.mmt.flights.entity.cancel.request.OrderCancelRQ;
import com.mmt.flights.entity.cancel.request.Query;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CancelPnrRequestAdapterTask implements MapTask {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        String supplierPNRResponse = flowState.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);

        // Convert supplier PNR response to OrderViewRS
        OrderViewRS retrieveResponse = objectMapper.readValue(supplierPNRResponse, OrderViewRS.class);

        // Create Cancel PNR Request
        CancelPnrRequest cancelPnrRequest = createCancelPnrRequest(retrieveResponse);

        // Convert to JSON
        String cancelPnrRequestJson = objectMapper.writeValueAsString(cancelPnrRequest);

        return flowState.toBuilder()
                .addValue(FlowStateKey.CANCEL_PNR_REQUEST, cancelPnrRequestJson)
                .build();
    }

    private CancelPnrRequest createCancelPnrRequest(OrderViewRS retrieveResponse) {
        CancelPnrRequest cancelPnrRequest = new CancelPnrRequest();
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
        contacts.setContact(Arrays.asList(contact));
        travelAgencySender.setContacts(contacts);

        sender.setTravelAgencySender(travelAgencySender);
        party.setSender(sender);
        orderCancelRQ.setParty(party);

        // Set Query - use split PNR details if available, otherwise use main PNR
        Query query = new Query();
        query.setOrderID(retrieveResponse.getOrder().get(0).getOrderID());
        query.setGdsBookingReference(new String[]{retrieveResponse.getOrder().get(0).getGdsBookingReference()});

        orderCancelRQ.setQuery(query);
        cancelPnrRequest.setOrderCancelRQ(orderCancelRQ);

        return cancelPnrRequest;
    }
}
