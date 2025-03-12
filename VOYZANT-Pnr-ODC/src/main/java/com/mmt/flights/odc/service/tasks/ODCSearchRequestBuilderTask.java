package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.common.Document;
import com.mmt.flights.entity.common.Party;
import com.mmt.flights.entity.common.Sender;
import com.mmt.flights.entity.common.TravelAgencySender;
import com.mmt.flights.entity.odc.*;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.search.Itinerary;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ODCSearchRequestBuilderTask implements MapTask {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public FlowState run(FlowState state) throws Exception {
        DateChangeSearchRequest request = state.getValue(FlowStateKey.REQUEST);
        String pnrResponseData = state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        OrderViewRS orderViewRS = objectMapper.readValue(pnrResponseData, OrderViewRS.class);
        if (request == null || request.getItineraryList() == null || request.getItineraryList().isEmpty()) {
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
        FlightQuery flightQuery = new FlightQuery();
        OriginDestinations originDest = new OriginDestinations();

        // Process each itinerary
        for (Itinerary itinerary : request.getItineraryList()) {
            OriginDestination od = new OriginDestination();
            // Previous flight details
            od.setPreviousDeparture(new AirportInfo(itinerary.getFrom(), itinerary.getDepDate().toString()));
            od.setPreviousArrival(new AirportInfo(itinerary.getTo(), null));
            od.setPreviousCabinType(request.getCabinClass());

            // New flight details
            od.setDeparture(new AirportInfo(itinerary.getFrom(), itinerary.getDepDate().toString()));
            od.setArrival(new AirportInfo(itinerary.getTo(), null));
            od.setCabinType(request.getCabinClass());

            originDest.getOriginDestination().add(od);
        }

        flightQuery.setOriginDestinations(originDest);
        servicing.getAdd().setFlightQuery(flightQuery);
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

        // Set preferences
        Preference preference = new Preference();
        preference.setCabinType(request.getCabinClass());
        rq.setPreference(preference);

        // Set metadata
        MetaData metadata = new MetaData();
        metadata.setTraceId(request.getMmtId());
        rq.setMetaData(metadata);

        orderReshopRequest.setOrderReshopRQ(rq);
        String orderReshopRequestStr = objectMapper.writeValueAsString(orderReshopRequest);

        return state.toBuilder()
                .addValue(FlowStateKey.ODC_SEARCH_REQUEST, orderReshopRequestStr)
                .build();
    }
}