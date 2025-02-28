package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.odc.*;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
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
        if (request == null) {
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

        OriginDestination od = new OriginDestination();
        // Previous flight details
        od.setPreviousDeparture(new Airport(request.getOriginAirport(), request.getOldDepartureDate()));
        od.setPreviousArrival(new Airport(request.getDestinationAirport(), null));
        od.setPreviousCabinType(request.getCabinClass());

        // New flight details
        od.setDeparture(new Airport(request.getOriginAirport(), request.getNewDepartureDate()));
        od.setArrival(new Airport(request.getDestinationAirport(), null));
        od.setCabinType(request.getCabinClass());

        originDest.getOriginDestination().add(od);
        flightQuery.setOriginDestinations(originDest);
        servicing.getAdd().setFlightQuery(flightQuery);
        reshop.setOrderServicing(servicing);
        query.setReshop(reshop);
        rq.setQuery(query);

        // Set passenger details
        DataLists dataLists = new DataLists();
        PassengerList passengerList = new PassengerList();
        for (DateChangeRequest.Passenger pax : request.getPassengers()) {
            Passenger passenger = new Passenger();
            passenger.setPassengerID(pax.getPassengerId());
            passenger.setPtc(pax.getPassengerType());
            passenger.setNameTitle(pax.getNameTitle());
            passenger.setFirstName(pax.getFirstName());
            passenger.setMiddleName(pax.getMiddleName());
            passenger.setLastName(pax.getLastName());
            passenger.setDocumentNumber(pax.getDocumentNumber());
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