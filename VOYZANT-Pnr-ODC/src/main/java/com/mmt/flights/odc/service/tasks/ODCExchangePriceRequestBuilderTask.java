package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.odc.*;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentRequest;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ODCExchangePriceRequestBuilderTask implements MapTask {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public FlowState run(FlowState state) throws Exception {
        DateChangePrePaymentRequest request = state.getValue(FlowStateKey.REQUEST);
        String pnrResponseData = state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        OrderViewRS orderViewRS = objectMapper.readValue(pnrResponseData, OrderViewRS.class);
        if (request == null || request.getRKey() == null || request.getRKey().isEmpty()) {
            throw new PSErrorException(ErrorEnum.INVALID_REQUEST);
        }

        // Build OrderReshopRQ
        OrderReshopRequest orderReshopRequest = new OrderReshopRequest();
        OrderReshopRQ rq = new OrderReshopRQ();

        // Set document info
        Document document = new Document();
        document.setName("API GATEWAY");
        document.setReferenceVersion("1.2");
        rq.setDocument(document);

        // Set party info
        Party party = new Party();
        Sender sender = new Sender();
        TravelAgencySender agencySender = new TravelAgencySender();
        agencySender.setName("MMT");
        agencySender.setAgencyID("MMT");
        sender.setTravelAgencySender(agencySender);
        party.setSender(sender);
        rq.setParty(party);

        // Set query details
        Query query = new Query();
        query.setOrderID(request.getMmtId());
        query.getGdsBookingReference().add(request.getPnr());

        // Set reshop details
        Reshop reshop = new Reshop();
        OrderServicing servicing = new OrderServicing();
        
        // Set qualifier with offer ID
        Add add = new Add();
        Qualifier qualifier = new Qualifier();
        ExistingOrderQualifier existingOrderQualifier = new ExistingOrderQualifier();
        OrderKeys orderKeys = new OrderKeys();
        
        // Add offer ID from journey keys if available
        if (request.getJourneyKeys() != null && !request.getJourneyKeys().isEmpty()) {
            for (String journeyKey : request.getJourneyKeys()) {
                Offer offer = new Offer();
                offer.setOfferID(journeyKey);
                orderKeys.getOffer().add(offer);
            }
        } else if (request.getChangedJourneys() != null && !request.getChangedJourneys().isEmpty()) {
            // Fall back to changed journeys if journey keys not available
            for (String changedJourney : request.getChangedJourneys()) {
                Offer offer = new Offer();
                offer.setOfferID(changedJourney);
                orderKeys.getOffer().add(offer);
            }
        }
        
        existingOrderQualifier.setOrderKeys(orderKeys);
        qualifier.setExistingOrderQualifier(existingOrderQualifier);
        add.setQualifier(qualifier);
        servicing.setAdd(add);
        
        reshop.setOrderServicing(servicing);
        query.setReshop(reshop);
        rq.setQuery(query);

        // Set passenger details
        DataLists dataLists = new DataLists();
        PassengerList passengerList = new PassengerList();
        for (com.mmt.flights.entity.pnr.retrieve.response.Passenger pax : orderViewRS.getDataLists().getPassengerList().getPassengers()) {
            Passenger passenger = new Passenger();
            passenger.setPassengerID(pax.getPassengerID());
            passenger.setPtc(pax.getPtc());
            passenger.setNameTitle(pax.getNameTitle());
            passenger.setFirstName(pax.getFirstName());
            passenger.setMiddleName(pax.getMiddleName());
            passenger.setLastName(pax.getLastName());
            passenger.setTravelDocument(pax.getTravelDocument());
            passengerList.getPassenger().add(passenger);
        }
        dataLists.setPassengerList(passengerList);
        rq.setDataLists(dataLists);

        // Set metadata
        MetaData metadata = new MetaData();
        metadata.setTraceId(request.getMmtId());
        rq.setMetaData(metadata);

        orderReshopRequest.setOrderReshopRQ(rq);
        String orderReshopRequestStr = objectMapper.writeValueAsString(orderReshopRequest);

        return state.toBuilder()
                .addValue(FlowStateKey.ODC_EXCHANGE_PRICE_REQUEST, orderReshopRequestStr)
                .build();
    }
}
