package com.mmt.flights.cancel.workflow.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.entity.cms.CMSMapHolder;
import com.mmt.flights.entity.pnr.retrieve.request.*;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CancelPnrRetrieveRequestAdapter implements MapTask {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        SupplyPnrCancelRequestDTO supplyPnrRequestDTO = flowState.getValue(FlowStateKey.REQUEST);
        CMSMapHolder cmsMap = flowState.getValue(FlowStateKey.CMS_MAP);

        OrderRetrieveRequest orderRetrieveRequest = new OrderRetrieveRequest();
        OrderRetreiveRQ orderRetreiveRQ = new OrderRetreiveRQ();
        
        // Set Document
        Document document = new Document();
        document.setName("Venkat B2B portal");
        document.setReferenceversion("1.0");
        orderRetreiveRQ.setDocument(document);
        
        // Set Party
        Party party = new Party();
        Sender sender = new Sender();
        TravelAgencySender travelAgencySender = new TravelAgencySender();
        travelAgencySender.setName("mrst");
        travelAgencySender.setIataNumber("");
        travelAgencySender.setAgencyId("");
        
        Contact contact = new Contact();
        contact.setEmailContact("pst@claritytts.com");
        Contacts contacts = new Contacts();
        contacts.setContact(Arrays.asList(contact));
        travelAgencySender.setContacts(contacts);
        
        sender.setTravelAgencySender(travelAgencySender);
        party.setSender(sender);
        orderRetreiveRQ.setParty(party);
        
        // Set Query
        Query query = new Query();
        query.setOrderId(supplyPnrRequestDTO.getRequestCore().getSupplierPnr());
        query.setGdsBookingReference(Arrays.asList(supplyPnrRequestDTO.getRequestCore().getSupplierPnr()));
        orderRetreiveRQ.setQuery(query);

        // Set the OrderRetreiveRQ in the wrapper
        orderRetrieveRequest.setOrderRetreiveRQ(orderRetreiveRQ);
        
        // Convert to JSON string
        String supplierPnrRequest = objectMapper.writeValueAsString(orderRetrieveRequest);

        return flowState.toBuilder()
                .addValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_REQUEST, supplierPnrRequest)
                .build();
    }
}
