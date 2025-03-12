package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.common.service.CommonDocumentService;
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
    
    @Autowired
    private CommonDocumentService commonDocumentService;

    @Override
    public FlowState run(FlowState state) throws Exception {
        DateChangePrePaymentRequest request = validateAndGetRequest(state);
        OrderViewRS orderViewRS = getOrderViewRS(state);
        
        OrderReshopRequest orderReshopRequest = new OrderReshopRequest();
        OrderReshopRQ rq = buildOrderReshopRQ(request, orderViewRS);
        orderReshopRequest.setOrderReshopRQ(rq);
        
        String orderReshopRequestStr = objectMapper.writeValueAsString(orderReshopRequest);
        return state.toBuilder()
                .addValue(FlowStateKey.ODC_EXCHANGE_PRICE_REQUEST, orderReshopRequestStr)
                .build();
    }

    private DateChangePrePaymentRequest validateAndGetRequest(FlowState state) throws PSErrorException {
        DateChangePrePaymentRequest request = state.getValue(FlowStateKey.REQUEST);
        if (request == null || request.getRKey() == null || request.getRKey().isEmpty()) {
            throw new PSErrorException(ErrorEnum.INVALID_REQUEST);
        }
        return request;
    }

    private OrderViewRS getOrderViewRS(FlowState state) throws Exception {
        String pnrResponseData = state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        return objectMapper.readValue(pnrResponseData, OrderViewRS.class);
    }

    private OrderReshopRQ buildOrderReshopRQ(DateChangePrePaymentRequest request, OrderViewRS orderViewRS) throws PSErrorException {
        OrderReshopRQ rq = new OrderReshopRQ();
        rq.setDocument(commonDocumentService.createDocument());
        rq.setParty(commonDocumentService.createParty());
        
        String[] rKeyParts = parseRKey(request.getRKey());
        rq.setShoppingResponseId(rKeyParts[0]);
        
        rq.setQuery(buildQuery(request, rKeyParts[1]));
        rq.setDataLists(buildDataLists(orderViewRS));
        rq.setMetaData(buildMetaData(request));
        
        return rq;
    }

    private String[] parseRKey(String rKey) throws PSErrorException {
        String[] rKeyParts = rKey.split(",");
        if (rKeyParts.length != 2) {
            throw new PSErrorException("Invalid rKey format", ErrorEnum.INVALID_REQUEST);
        }
        return rKeyParts;
    }

    private Query buildQuery(DateChangePrePaymentRequest request, String offerId) {
        Query query = new Query();
        query.setOrderID(request.getMmtId());
        query.getGdsBookingReference().add(request.getPnr());
        query.setReshop(buildReshop(offerId));
        return query;
    }

    private Reshop buildReshop(String offerId) {
        Reshop reshop = new Reshop();
        OrderServicing servicing = new OrderServicing();
        servicing.setAdd(buildAdd(offerId));
        reshop.setOrderServicing(servicing);
        return reshop;
    }

    private Add buildAdd(String offerId) {
        Add add = new Add();
        Qualifier qualifier = new Qualifier();
        ExistingOrderQualifier existingOrderQualifier = new ExistingOrderQualifier();
        
        OrderKeys orderKeys = new OrderKeys();
        Offer offer = new Offer();
        offer.setOfferID(offerId);
        orderKeys.getOffer().add(offer);
        
        existingOrderQualifier.setOrderKeys(orderKeys);
        qualifier.setExistingOrderQualifier(existingOrderQualifier);
        add.setQualifier(qualifier);
        
        return add;
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

    private MetaData buildMetaData(DateChangePrePaymentRequest request) {
        MetaData metadata = new MetaData();
        metadata.setTraceId(request.getMmtId());
        return metadata;
    }
}
