package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.common.service.CommonDocumentService;
import com.mmt.flights.entity.common.*;
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
    
    @Autowired
    private CommonDocumentService commonDocumentService;

    @Override
    public FlowState run(FlowState state) throws Exception {
        DateChangeSearchRequest request = validateAndGetRequest(state);
        OrderViewRS orderViewRS = getOrderViewRS(state);
        
        OrderReshopRequest orderReshopRequest = new OrderReshopRequest();
        OrderReshopRQ rq = buildOrderReshopRQ(request, orderViewRS);
        orderReshopRequest.setOrderReshopRQ(rq);
        
        String orderReshopRequestStr = objectMapper.writeValueAsString(orderReshopRequest);
        return state.toBuilder()
                .addValue(FlowStateKey.ODC_SEARCH_REQUEST, orderReshopRequestStr)
                .build();
    }

    private DateChangeSearchRequest validateAndGetRequest(FlowState state) throws PSErrorException {
        DateChangeSearchRequest request = state.getValue(FlowStateKey.REQUEST);
        if (request == null || request.getItineraryList() == null || request.getItineraryList().isEmpty()) {
            throw new PSErrorException(ErrorEnum.INVALID_REQUEST);
        }
        return request;
    }

    private OrderViewRS getOrderViewRS(FlowState state) throws Exception {
        String pnrResponseData = state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        return objectMapper.readValue(pnrResponseData, OrderViewRS.class);
    }

    private OrderReshopRQ buildOrderReshopRQ(DateChangeSearchRequest request, OrderViewRS orderViewRS) {
        OrderReshopRQ rq = new OrderReshopRQ();
        rq.setDocument(commonDocumentService.createDocument());
        rq.setParty(commonDocumentService.createParty());
        rq.setQuery(buildQuery(request));
        rq.setDataLists(buildDataLists(orderViewRS));
        rq.setPreference(buildPreference(request));
        rq.setMetaData(buildMetaData(request));
        return rq;
    }

    private Query buildQuery(DateChangeSearchRequest request) {
        Query query = new Query();
        query.setOrderID(request.getMmtId());
        query.getGdsBookingReference().add(request.getPnr());
        query.setReshop(buildReshop(request));
        return query;
    }

    private Reshop buildReshop(DateChangeSearchRequest request) {
        Reshop reshop = new Reshop();
        OrderServicing servicing = new OrderServicing();
        servicing.getAdd().setFlightQuery(buildFlightQuery(request));
        reshop.setOrderServicing(servicing);
        return reshop;
    }

    private FlightQuery buildFlightQuery(DateChangeSearchRequest request) {
        FlightQuery flightQuery = new FlightQuery();
        OriginDestinations originDest = new OriginDestinations();
        
        for (Itinerary itinerary : request.getItineraryList()) {
            originDest.getOriginDestination().add(buildOriginDestination(itinerary, request.getCabinClass()));
        }
        
        flightQuery.setOriginDestinations(originDest);
        return flightQuery;
    }

    private OriginDestination buildOriginDestination(Itinerary itinerary, String cabinClass) {
        OriginDestination od = new OriginDestination();
        od.setPreviousDeparture(new AirportInfo(itinerary.getFrom(), itinerary.getDepDate().toString()));
        od.setPreviousArrival(new AirportInfo(itinerary.getTo(), null));
        od.setPreviousCabinType(cabinClass);
        
        od.setDeparture(new AirportInfo(itinerary.getFrom(), itinerary.getDepDate().toString()));
        od.setArrival(new AirportInfo(itinerary.getTo(), null));
        od.setCabinType(cabinClass);
        
        return od;
    }

    private DataLists buildDataLists(OrderViewRS orderViewRS) {
        DataLists dataLists = new DataLists();
        PassengerList passengerList = new PassengerList();
        
        for (com.mmt.flights.entity.pnr.retrieve.response.Passenger pax : orderViewRS.getDataLists().getPassengerList().getPassengers()) {
            passengerList.getPassenger().add(buildPassenger(pax));
        }
        
        dataLists.setPassengerList(passengerList);
        return dataLists;
    }

    private Passenger buildPassenger(com.mmt.flights.entity.pnr.retrieve.response.Passenger pax) {
        Passenger passenger = new Passenger();
        passenger.setPassengerID(pax.getPassengerID());
        passenger.setPtc(pax.getPtc());
        passenger.setNameTitle(pax.getNameTitle());
        passenger.setFirstName(pax.getFirstName());
        passenger.setMiddleName(pax.getMiddleName());
        passenger.setLastName(pax.getLastName());
        passenger.setTravelDocument(pax.getTravelDocument());
        return passenger;
    }

    private Preference buildPreference(DateChangeSearchRequest request) {
        Preference preference = new Preference();
        preference.setCabinType(request.getCabinClass());
        return preference;
    }

    private MetaData buildMetaData(DateChangeSearchRequest request) {
        MetaData metadata = new MetaData();
        metadata.setTraceId(request.getMmtId());
        return metadata;
    }
}