package com.mmt.flights.cancel.workflow.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.entity.cms.CMSMapHolder;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.entity.cancel.request.CancelPnrRequest;
import com.mmt.flights.entity.cancel.request.OrderCancelRQ;
import com.mmt.flights.entity.cancel.request.Query;
import com.mmt.flights.entity.cancel.common.Document;
import com.mmt.flights.entity.cancel.common.Party;
import com.mmt.flights.entity.cancel.common.Sender;
import com.mmt.flights.entity.cancel.common.TravelAgencySender;
import com.mmt.flights.entity.cancel.common.Contacts;
import com.mmt.flights.entity.cancel.common.Contact;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

@Component
public class CancelPnrRequestAdapterTask implements MapTask {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        SupplyPnrCancelRequestDTO request = flowState.getValue(FlowStateKey.REQUEST);
        String supplierPNRResponse = flowState.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        CMSMapHolder cmsMap = flowState.getValue(FlowStateKey.CMS_MAP);

        // Convert supplier PNR response to OrderViewRS
        OrderViewRS retrieveResponse = objectMapper.readValue(supplierPNRResponse, OrderViewRS.class);

        // Create Cancel PNR Request
        CancelPnrRequest cancelPnrRequest = createCancelPnrRequest(retrieveResponse, request);

        // Convert to JSON
        String cancelPnrRequestJson = objectMapper.writeValueAsString(cancelPnrRequest);

        return flowState.toBuilder()
                .addValue(FlowStateKey.CANCEL_PNR_REQUEST, cancelPnrRequestJson)
                .build();
    }

    private CancelPnrRequest createCancelPnrRequest(OrderViewRS retrieveResponse, SupplyPnrCancelRequestDTO request) {
        CancelPnrRequest cancelRequest = new CancelPnrRequest();
        OrderCancelRQ orderCancelRQ = new OrderCancelRQ();

        // Set Document
        Document document = new Document();
        document.setName("Skyroute B2B Portal");
        document.setReferenceVersion("1.0");
        orderCancelRQ.setDocument(document);

        // Set Party
        Party party = new Party();
        Sender sender = new Sender();
        TravelAgencySender travelAgencySender = new TravelAgencySender();
        travelAgencySender.setName("Skyroute B2B");
        travelAgencySender.setIataNumber("1111111111");
        travelAgencySender.setAgencyID("1111111111");

        // Set Contacts
        Contacts contacts = new Contacts();
        Contact contact = new Contact();
        contact.setEmailContact("pst@claritytts.com");
        contacts.setContact(Arrays.asList(contact));
        travelAgencySender.setContacts(contacts);

        sender.setTravelAgencySender(travelAgencySender);
        party.setSender(sender);
        orderCancelRQ.setParty(party);

        // Set Query
        Query query = new Query();
        if (retrieveResponse != null && !retrieveResponse.getOrder().isEmpty()) {
            query.setOrderID(retrieveResponse.getOrder().get(0).getOrderID());
            query.setGdsBookingReference(new String[]{retrieveResponse.getOrder().get(0).getGdsBookingReference()});
        } else {
            // Fallback to request values if available
            query.setOrderID(request.getRequestCore().getSupplierPnr());
            query.setGdsBookingReference(new String[]{request.getRequestCore().getSupplierPnr()});
        }
        orderCancelRQ.setQuery(query);

        cancelRequest.setOrderCancelRQ(orderCancelRQ);
        return cancelRequest;
    }
}
