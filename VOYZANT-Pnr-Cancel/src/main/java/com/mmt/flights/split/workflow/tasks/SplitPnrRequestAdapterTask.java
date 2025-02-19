package com.mmt.flights.split.workflow.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.entity.split.request.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SplitPnrRequestAdapterTask implements MapTask {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        String supplierPNRResponse = flowState.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);

        // Convert supplier PNR response to OrderViewRS
        OrderViewRS retrieveResponse = objectMapper.readValue(supplierPNRResponse, OrderViewRS.class);

        // Create Split PNR Request
        AirSplitPnrRequest splitPnrRequest = createSplitPnrRequest(retrieveResponse);

        // Convert to JSON
        String splitPnrRequestJson = objectMapper.writeValueAsString(splitPnrRequest);

        return flowState.toBuilder()
                .addValue(FlowStateKey.SPLIT_PNR_REQUEST, splitPnrRequestJson)
                .build();
    }

    private AirSplitPnrRequest createSplitPnrRequest(OrderViewRS retrieveResponse) {
        AirSplitPnrRequest splitRequest = new AirSplitPnrRequest();
        AirSplitPnrRQ airSplitPnrRQ = new AirSplitPnrRQ();

        // Set Document
        Document document = new Document();
        document.setName("Skyroute B2B Portal");
        document.setReferenceVersion("1.0");
        airSplitPnrRQ.setDocument(document);

        // Set Party
        Party party = new Party();
        Sender sender = new Sender();
        TravelAgencySender travelAgencySender = new TravelAgencySender();
        travelAgencySender.setName("Skyroute B2B");
        travelAgencySender.setIataNumber("1111111111");
        travelAgencySender.setAgencyId("1111111111");

        // Set Contacts
        Contacts contacts = new Contacts();
        Contact contact = new Contact();
        contact.setEmailContact("pst@claritytts.com");
        contacts.setContact(Arrays.asList(contact));
        travelAgencySender.setContacts(contacts);

        sender.setTravelAgencySender(travelAgencySender);
        party.setSender(sender);
        airSplitPnrRQ.setParty(party);

        // Set Query
        Query query = new Query();
        if (retrieveResponse != null && !retrieveResponse.getOrder().isEmpty()) {
            query.setOrderId(retrieveResponse.getOrder().get(0).getOrderID());
            query.setGdsBookingReference(retrieveResponse.getOrder().get(0).getGdsBookingReference());
        }
        airSplitPnrRQ.setQuery(query);

        // Set DataLists with PassengerList
        DataLists dataLists = new DataLists();
        PassengerList passengerList = new PassengerList();
        
        if (retrieveResponse.getDataLists() != null && retrieveResponse.getDataLists().getPassengerList() != null) {
            List<Passenger> passengers = retrieveResponse.getDataLists().getPassengerList().getPassengers().stream()
                    .map(pax -> {
                        Passenger passenger = new Passenger();
                        passenger.setPassengerId(pax.getPassengerID());
                        passenger.setPtc(pax.getPtc());
                        passenger.setFirstName(pax.getFirstName());
                        passenger.setLastName(pax.getLastName());
                        return passenger;
                    })
                    .collect(Collectors.toList());
            
            passengerList.setPassenger(passengers);
        }
        
        dataLists.setPassengerList(passengerList);
        airSplitPnrRQ.setDataLists(dataLists);

        splitRequest.setAirSplitPnrRQ(airSplitPnrRQ);
        return splitRequest;
    }
}